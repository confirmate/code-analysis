/*
 * This file is part of the Confirmate project.
 */
package de.fraunhofer.aisec.confirmate.queries

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Interprocedural
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.concepts.crypto.encryption.Cipher
import de.fraunhofer.aisec.cpg.graph.concepts.crypto.encryption.Encrypt
import de.fraunhofer.aisec.cpg.graph.concepts.file.WriteFile
import de.fraunhofer.aisec.cpg.query.GenericQueryOperators
import de.fraunhofer.aisec.cpg.query.QueryTree
import de.fraunhofer.aisec.cpg.query.allExtended
import de.fraunhofer.aisec.cpg.query.alwaysComesFrom

/**
 * This query checks whether each path leading to a persistent data sink encrypts the data before
 * writing it to the sink. By default, it considers [WriteFile] nodes as persistent sinks and looks
 * for [Encrypt] nodes in the data and execution flow. What is considered as a persistent sink and
 * how to extract the written data can be customized by providing the [isPersistentSink] and
 * [writtenData] functions, respectively.
 */
context(translationResult: TranslationResult)
fun dataEncryptedBeforePersisting(
    isPersistentSink: (Node) -> Boolean = { it is WriteFile },
    writtenData: (Node) -> Node? = { (it as? WriteFile)?.what },
): QueryTree<Boolean> {
    return translationResult.allExtended<Node>(isPersistentSink) {
        val writtenData =
            writtenData(it)
                ?: return@allExtended QueryTree(
                    value = false,
                    stringRepresentation = "Missing data written to sink",
                    node = it,
                    operator = GenericQueryOperators.EVALUATE,
                )

        writtenData.alwaysComesFrom(scope = Interprocedural(), predicate = { it is Encrypt })
    }
}

fun Cipher.conformsToStateOfTheArt(): QueryTree<Boolean> {
    if("AES" in (cipherName?.uppercase() ?: "")) {
        return checkAES()
    }

    return QueryTree(
        value = false,
        stringRepresentation =
                "Cipher ${this.cipherName} with key size ${this.keySize} is NOT considered state of the art",
        node = this,
        operator = GenericQueryOperators.EVALUATE,
    )
}
