/*
 * This file is part of the Confirmate project.
 */
package de.fraunhofer.aisec.confirmate.queries.cra

import de.fraunhofer.aisec.confirmate.queries.catalogs.CryptoCatalog
import de.fraunhofer.aisec.confirmate.queries.catalogs.german.SymmetricCipher
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.assumptions.AssumptionType
import de.fraunhofer.aisec.cpg.assumptions.assume
import de.fraunhofer.aisec.cpg.graph.Interprocedural
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.concepts.crypto.encryption.Cipher
import de.fraunhofer.aisec.cpg.graph.concepts.crypto.encryption.Encrypt
import de.fraunhofer.aisec.cpg.graph.concepts.file.WriteFile
import de.fraunhofer.aisec.cpg.graph.concepts.http.HttpEndpointOperation
import de.fraunhofer.aisec.cpg.graph.concepts.http.HttpRequest
import de.fraunhofer.aisec.cpg.query.GenericQueryOperators
import de.fraunhofer.aisec.cpg.query.QueryTree
import de.fraunhofer.aisec.cpg.query.allExtended
import de.fraunhofer.aisec.cpg.query.alwaysComesFrom
import de.fraunhofer.aisec.cpg.query.and
import de.fraunhofer.aisec.cpg.query.mergeWithAll
import de.fraunhofer.aisec.cpg.query.or

/**
 * This query checks whether each path leading to a persistent data sink encrypts the data before
 * writing it to the sink. By default, it considers [WriteFile] nodes as persistent sinks and looks
 * for [Encrypt] nodes in the data and execution flow. What is considered as a persistent sink and
 * how to extract the written data can be customized by providing the [isPersistentSink] and
 * [writtenData] functions, respectively.
 */
context(translationResult: TranslationResult, cryptoCatalog: CryptoCatalog)
fun dataEncryptedBeforePersisting(
    isPersistentSink: (Node) -> Boolean = { it is WriteFile },
    writtenData: (Node) -> Node? = { (it as? WriteFile)?.what },
): QueryTree<Boolean> {
    return translationResult.allExtended<Node>(isPersistentSink) {
        val writtenData =
            writtenData(it)
                ?: return@allExtended QueryTree(
                        value = true,
                        stringRepresentation = "Missing data written to a persistent location",
                        node = it,
                        operator = GenericQueryOperators.EVALUATE,
                    )
                    .assume(
                        AssumptionType.CompletenessAssumption,
                        "We did not find the data written to the persistent sink which might lead to false positives. This behavior is strange as the sink should typically have a way to persist data.\n\nTo verify this assumption, please check if the sink has indeed no way to persist data. To fix this issue, either specify the way how to identify data (by providing an improved `writtenData` argument or consider removing the sink from the `isPersistentSink` argument.",
                    )
        writtenData.alwaysCorrectlyEncrypted()
    }
}

context(cryptoCatalog: CryptoCatalog)
fun Node.alwaysCorrectlyEncrypted(): QueryTree<Boolean> {
    val relevantEncryptOperations = mutableListOf<Encrypt>()
    val writtenDataIsEncrypted =
        this.alwaysComesFrom(
            scope = Interprocedural(),
            predicate = { enc ->
                if (enc is Encrypt) {
                    relevantEncryptOperations.add(enc)
                    true
                } else {
                    false
                }
            },
        )

    // The data must be encrypted and the encryption must be state of the art
    return writtenDataIsEncrypted and
        relevantEncryptOperations.map { it.concept.conformsToStateOfTheArt() }.mergeWithAll()
}

context(cryptoCatalog: CryptoCatalog)
fun Cipher.conformsToStateOfTheArt(): QueryTree<Boolean> {
    if (this is SymmetricCipher) {
        return cryptoCatalog.checkSymmetricEncryption()
    }

    return QueryTree(
        value = false,
        stringRepresentation =
            "Cipher ${this.cipherName} with key size ${this.keySize} is NOT considered state of the art",
        node = this,
        operator = GenericQueryOperators.EVALUATE,
    )
}

context(translationResult: TranslationResult)
fun dataInTransitEncrypted(): QueryTree<Boolean> {

    TODO()
}

context(translationResult: TranslationResult, cryptoCatalog: CryptoCatalog)
fun secureHttpRequests(): QueryTree<Boolean> =
    translationResult.allExtended<HttpRequest> {
        // Check if the request is sent over TLS. That's already a good starting point.
        // TODO: We should actually check the exact TLS configuration (i.e., version, ciphers, ...),
        // but that's not modeled in the CPG yet.
        val isSecureChannel = it.concept.isTLS
        val secureChannel =
            QueryTree(
                value = isSecureChannel == true,
                stringRepresentation =
                    if (isSecureChannel == true) {
                        "Data is sent over a secure channel (TLS)"
                    } else {
                        "Data is sent over an insecure channel (no TLS)"
                    },
                node = it,
                operator = GenericQueryOperators.EVALUATE,
            )

        // Check if all arguments of the request are encrypted before the request is sent
        val dataEncryptedBeforeRequest =
            it.arguments.map { arg -> arg.alwaysCorrectlyEncrypted() }.mergeWithAll()

        // We need a secure channel or the data must be encrypted before sending
        secureChannel or dataEncryptedBeforeRequest
    }

context(translationResult: TranslationResult, cryptoCatalog: CryptoCatalog)
fun secureHttpResponses(): QueryTree<Boolean> =
    // We start with the HttpEndpointOperations as they represent the server-side handling of
    // requests
    translationResult.allExtended<HttpEndpointOperation> {
        // Get the HttpClient and check if it's configured to use (only) TLS.
        val isSecureChannel = it.concept.isTLS
        val secureChannel =
            QueryTree(
                value = isSecureChannel == true,
                stringRepresentation =
                    if (isSecureChannel == true) {
                        "Data is sent over a secure channel (TLS)"
                    } else {
                        "Data is sent over an insecure channel (no TLS)"
                    },
                node = it,
                operator = GenericQueryOperators.EVALUATE,
            )

        // Check if data sent via the endpoint (i.e., the responses) are encrypted before the
        // response is sent
        val dataEncryptedBeforeResponse =
            it.prevDFG.map { arg -> arg.alwaysCorrectlyEncrypted() }.mergeWithAll()

        // We need a secure channel or the data must be encrypted before sending
        secureChannel or dataEncryptedBeforeResponse
    }
