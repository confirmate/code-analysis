/*
 * This file is part of the Confirmate project.
 */
package de.fraunhofer.aisec.confirmate.queries.catalogs.german

import de.fraunhofer.aisec.confirmate.queries.HybridCipher
import de.fraunhofer.aisec.confirmate.queries.RngGet
import de.fraunhofer.aisec.confirmate.queries.SymmetricCipher
import de.fraunhofer.aisec.cpg.graph.Backward
import de.fraunhofer.aisec.cpg.graph.ContextSensitive
import de.fraunhofer.aisec.cpg.graph.FieldSensitive
import de.fraunhofer.aisec.cpg.graph.GraphToFollow
import de.fraunhofer.aisec.cpg.graph.Interprocedural
import de.fraunhofer.aisec.cpg.graph.concepts.crypto.encryption.Cipher
import de.fraunhofer.aisec.cpg.query.GenericQueryOperators
import de.fraunhofer.aisec.cpg.query.Must
import de.fraunhofer.aisec.cpg.query.QueryTree
import de.fraunhofer.aisec.cpg.query.and
import de.fraunhofer.aisec.cpg.query.dataFlow
import de.fraunhofer.aisec.cpg.query.mergeWithAll
import de.fraunhofer.aisec.cpg.query.mergeWithAny
import kotlin.collections.contains

/*
 * This file defines queries for analyzing cryptographic practices in code using the CPG (Code Property Graph) framework.
 * As a reference, we use the BSI TR02102 standard (https://www.bsi.bund.de/SharedDocs/Downloads/DE/BSI/Publikationen/TechnischeRichtlinien/TR02102/TR02102.html).
 */

// Symmetric encryption algorithms, their modus, and recommended key lengths. There are also
// requirements on the IV, and interdependencies between accepted key lengths and modus.

internal fun SymmetricCipher.checkAES(): QueryTree<Boolean> {
    return listOf(::checkAesCCM, ::checkAesGCM, ::checkAesGcmSiv, ::checkAesCBC, ::checkAesCTR)
        .map { it.invoke() }
        .mergeWithAny(this)
}

internal fun SymmetricCipher.isAES(): QueryTree<Boolean> {
    val algoAccepted = this.cipherName?.uppercase()?.contains("AES") == true
    val algoCheck =
        QueryTree(
            value = algoAccepted,
            stringRepresentation =
                if (algoAccepted) {
                    "Algorithm is AES"
                } else {
                    "Algorithm $cipherName is not AES"
                },
            node = this,
            operator = GenericQueryOperators.EVALUATE,
        )
    return algoCheck
}

internal fun SymmetricCipher.isModus(modus: String): QueryTree<Boolean> {
    val modusAccepted = this.cipherName?.uppercase()?.contains(modus) == true
    val modusCheck =
        QueryTree(
            value = modusAccepted,
            stringRepresentation =
                if (modusAccepted) {
                    "Modus is $modus"
                } else {
                    "Modus $cipherName is not $modus"
                },
            node = this,
            operator = GenericQueryOperators.EVALUATE,
        )
    return modusCheck
}

internal fun SymmetricCipher.keyIsBlockSize(): QueryTree<Boolean> {
    val keySizeIsBlockSize = this.keySize == this.blockSize
    val keyIsBlockSizeCheck =
        QueryTree(
            value = keySizeIsBlockSize,
            stringRepresentation =
                if (keySizeIsBlockSize) {
                    "Key size matches block size"
                } else {
                    "Key size $keySize does not match block size $blockSize"
                },
            node = this,
            operator = GenericQueryOperators.EVALUATE,
        )
    return keyIsBlockSizeCheck
}

internal fun SymmetricCipher.validKeyLength(keySizes: Collection<Int>): QueryTree<Boolean> {
    val validKeyLength = this.keySize in keySizes
    val validKeyLengthCheck =
        QueryTree(
            value = validKeyLength,
            stringRepresentation =
                if (validKeyLength) {
                    "Key size is in the list of allowed values: $keySizes"
                } else {
                    "Key size $keySize is not in the list of allowed values: $keySizes"
                },
            node = this,
            operator = GenericQueryOperators.EVALUATE,
        )
    return validKeyLengthCheck
}

internal fun SymmetricCipher.validAuthTagSize(minimalAuthTagSize: Int): QueryTree<Boolean> {
    val tagSizeAccepted =
        (this.tagSize
            ?: return QueryTree(
                value = false,
                stringRepresentation = "Unknown tag size for cipher",
                node = this,
                operator = GenericQueryOperators.EVALUATE,
            )) >= minimalAuthTagSize
    val tagSizeCheck =
        QueryTree(
            value = tagSizeAccepted,
            stringRepresentation =
                if (tagSizeAccepted) {
                    "The authentication tag length ${this.tagSize} is at least $minimalAuthTagSize bits"
                } else {
                    "The authentication tag length ${this.tagSize} is smaller than $minimalAuthTagSize bits"
                },
            node = this,
            operator = GenericQueryOperators.EVALUATE,
        )
    return tagSizeCheck
}

internal fun SymmetricCipher.validIvSize(minimalIvSize: Int): QueryTree<Boolean> {
    val ivSizeAccepted =
        (this.ivSize
            ?: return QueryTree(
                value = false,
                stringRepresentation = "Unknown IV size for cipher",
                node = this,
                operator = GenericQueryOperators.EVALUATE,
            )) >= minimalIvSize
    val ivSizeCheck =
        QueryTree(
            value = ivSizeAccepted,
            stringRepresentation =
                if (ivSizeAccepted) {
                    "The IV length ${this.ivSize} is at least $minimalIvSize bits"
                } else {
                    "The IV length ${this.ivSize} is smaller than $minimalIvSize bits"
                },
            node = this,
            operator = GenericQueryOperators.EVALUATE,
        )
    return ivSizeCheck
}

internal fun SymmetricCipher.ivSizeEqualsBlockSize(): QueryTree<Boolean> {
    val ivSizeAccepted = this.ivSize == blockSize
    val ivSizeCheck =
        QueryTree(
            value = ivSizeAccepted,
            stringRepresentation =
                if (ivSizeAccepted) {
                    "The IV length ${this.ivSize} is the same as the block size $blockSize bits"
                } else {
                    "The IV length ${this.ivSize} is different than the block size $blockSize bits"
                },
            node = this,
            operator = GenericQueryOperators.EVALUATE,
        )
    return ivSizeCheck
}

internal fun SymmetricCipher.isUniqueIv(): QueryTree<Boolean> {
    // TODO: We cannot check this statically, but it is a requirement that the IV is unique for each
    // encryption operation with the same key.
    val ivIsUnique = true
    val ivIsUniqueCheck =
        QueryTree(
            value = ivIsUnique,
            stringRepresentation =
                if (ivIsUnique) "The IV is unique for each encryption operation with the same key"
                else "The IV is not unique for each encryption operation with the same key",
            node = this,
            operator = GenericQueryOperators.EVALUATE,
        )
    return ivIsUniqueCheck
}

internal fun SymmetricCipher.isRandomIv(): QueryTree<Boolean> {
    val iv =
        this.iv
            ?: return QueryTree(
                value = false,
                stringRepresentation = "No IV is set for the cipher but required",
                node = this,
                operator = GenericQueryOperators.EVALUATE,
            )

    return dataFlow(
        iv,
        direction = Backward(GraphToFollow.DFG),
        type = Must,
        sensitivities = FieldSensitive + ContextSensitive,
        scope = Interprocedural(),
        predicate = { it is RngGet },
    )
}

internal fun SymmetricCipher.checkAesCCM(): QueryTree<Boolean> {
    val algoCheck = this.isAES()
    val modusCheck = this.isModus("CCM")
    val keyIsBlockSizeCheck = this.keyIsBlockSize()
    // Note: The TR itself has no limitation on the key size for AES-CCM, but it references NIST SP
    // 800-38C, which only defines AES-CCM for key sizes of 128 bits.
    val validKeyLengthCheck = this.validKeyLength(listOf(128))
    // Tag size must be at least 96 bits
    val tagSizeCheck = this.validAuthTagSize(96)
    // TODO: Not sure why the AI suggests me this value. The TR doesn't state anything.
    val ivLengthCheck = this.validIvSize(104)
    val ivIsUniqueCheck = this.isUniqueIv()

    val allChecks =
        listOf(
            algoCheck,
            modusCheck,
            keyIsBlockSizeCheck,
            validKeyLengthCheck,
            tagSizeCheck,
            ivLengthCheck,
            ivIsUniqueCheck,
        )
    val allChecksPassed = allChecks.all { it.value }

    return QueryTree(
        value = allChecksPassed,
        children = allChecks,
        stringRepresentation = "Checks for AES-CCM ${if(allChecksPassed) "passed" else "failed" }.",
        node = this,
        operator = GenericQueryOperators.ALL,
    )
}

internal fun SymmetricCipher.checkAesGCM(): QueryTree<Boolean> {
    val algoCheck = this.isAES()
    val modusCheck = this.isModus("GCM")
    val keyIsBlockSizeCheck = this.keyIsBlockSize()
    val validKeyLengthCheck = this.validKeyLength(listOf(128, 192, 256))
    // Tag size must be at least 96 bits
    val tagSizeCheck = this.validAuthTagSize(96)
    val ivLengthCheck = this.validIvSize(96)
    val ivIsUniqueCheck = this.isUniqueIv()
    val ivIsRandom = this.isRandomIv()

    val allChecks =
        listOf(
            algoCheck,
            modusCheck,
            keyIsBlockSizeCheck,
            validKeyLengthCheck,
            tagSizeCheck,
            ivLengthCheck,
            ivIsUniqueCheck,
        )
    val allChecksPassed = allChecks.all { it.value }

    return QueryTree(
        value = allChecksPassed,
        children = allChecks,
        stringRepresentation = "Checks for AES-GCM ${if(allChecksPassed) "passed" else "failed" }.",
        node = this,
        operator = GenericQueryOperators.ALL,
    )
}

internal fun SymmetricCipher.checkAesGcmSiv(): QueryTree<Boolean> {
    val algoCheck = this.isAES()
    val modusCheck = this.isModus("GCM-SIV")
    val keyIsBlockSizeCheck = this.keyIsBlockSize()
    val validKeyLengthCheck = this.validKeyLength(listOf(128, 256))
    // Tag size must be at least 96 bits
    val tagSizeCheck = this.validAuthTagSize(96)
    val ivLengthCheck = this.validIvSize(96)
    val ivIsUniqueCheck = this.isUniqueIv()
    val ivIsRandomCheck = this.isRandomIv()

    val allChecks =
        listOf(
            algoCheck,
            modusCheck,
            keyIsBlockSizeCheck,
            validKeyLengthCheck,
            tagSizeCheck,
            ivLengthCheck,
            ivIsUniqueCheck,
            ivIsRandomCheck,
        )
    val allChecksPassed = allChecks.all { it.value }

    return QueryTree(
        value = allChecksPassed,
        children = allChecks,
        stringRepresentation =
            "Checks for AES-GCM-SIV ${if(allChecksPassed) "passed" else "failed" }.",
        node = this,
        operator = GenericQueryOperators.ALL,
    )
}

internal fun SymmetricCipher.checkAesCBC(): QueryTree<Boolean> {
    val algoCheck = this.isAES()
    val modusCheck = this.isModus("CBC")
    val keyIsBlockSizeCheck = this.keyIsBlockSize()
    val validKeyLengthCheck = this.validKeyLength(listOf(128, 192, 256))
    val ivLengthCheck = this.ivSizeEqualsBlockSize()
    val ivIsRandomCheck = this.isRandomIv()

    // TODO: There are requirements concerning padding!

    val allChecks =
        listOf(
            algoCheck,
            modusCheck,
            keyIsBlockSizeCheck,
            validKeyLengthCheck,
            ivLengthCheck,
            ivIsRandomCheck,
        )
    val allChecksPassed = allChecks.all { it.value }

    return QueryTree(
        value = allChecksPassed,
        children = allChecks,
        stringRepresentation = "Checks for AES-CBC ${if(allChecksPassed) "passed" else "failed" }.",
        node = this,
        operator = GenericQueryOperators.ALL,
    )
}

internal fun SymmetricCipher.checkAesCTR(): QueryTree<Boolean> {
    val algoCheck = this.isAES()
    val modusCheck = this.isModus("CTR")
    val keyIsBlockSizeCheck = this.keyIsBlockSize()
    val validKeyLengthCheck = this.validKeyLength(listOf(128, 192, 256))
    val ivLengthCheck = this.ivSizeEqualsBlockSize()
    val ivIsUniqueCheck = this.isUniqueIv()

    // TODO: There are requirements concerning padding!

    val allChecks =
        listOf(
            algoCheck,
            modusCheck,
            keyIsBlockSizeCheck,
            validKeyLengthCheck,
            ivLengthCheck,
            ivIsUniqueCheck,
        )
    val allChecksPassed = allChecks.all { it.value }

    return QueryTree(
        value = allChecksPassed,
        children = allChecks,
        stringRepresentation = "Checks for AES-CTR ${if(allChecksPassed) "passed" else "failed" }.",
        node = this,
        operator = GenericQueryOperators.ALL,
    )
}

// Checking asymmetric crypto

internal fun Cipher.checkRSA(): QueryTree<Boolean> {
    val isRSA =
        QueryTree(
            value = this.cipherName == "RSA",
            stringRepresentation =
                if (cipherName == "RSA") "The algorithm is RSA" else "The algorithm is not RSA",
            node = this,
            operator = GenericQueryOperators.EVALUATE,
        )
    val keysizeOk =
        this.keySize?.let {
            it
            QueryTree(
                value = it >= 3000,
                stringRepresentation =
                    if (it >= 3000) "The keysize is bigger than 3000 bit"
                    else "The keysize $it is smaller than the required 3000 bit",
                node = this,
                operator = GenericQueryOperators.EVALUATE,
            )
        }
            ?: QueryTree(
                value = false,
                stringRepresentation = "Could not identify the keysize",
                node = this,
                operator = GenericQueryOperators.EVALUATE,
            )
    return isRSA and keysizeOk
}

context(catalog: GermanCatalog)
internal fun Cipher.checkDLIES(): QueryTree<Boolean> {
    if (this !is HybridCipher) {
        return QueryTree(
            value = false,
            stringRepresentation = "The algorithm is not a hybrid cipher, so it cannot be DLIES",
            node = this,
            operator = GenericQueryOperators.EVALUATE,
        )
    }

    val isDLIES =
        QueryTree(
            value = this.cipherName == "DLIES",
            stringRepresentation =
                if (cipherName == "DLIES") "The algorithm is DLIES"
                else "The algorithm is not DLIES",
            node = this,
            operator = GenericQueryOperators.EVALUATE,
        )

    val symmetricCipherOk =
        this.symmetricCipher?.let { symmetricCipher ->
            with(symmetricCipher) { catalog.checkSymmetricEncryption() }
        }
            ?: QueryTree(
                value = false,
                stringRepresentation =
                    "Could not identify the symmetric cipher with the hybrid algorithm",
                node = this,
                operator = GenericQueryOperators.EVALUATE,
            )

    val hashFunctionOk =
        this.hashFunction?.let { hashFunction ->
            with(hashFunction) { catalog.checkHashFunction() }
        }
            ?: QueryTree(
                value = false,
                stringRepresentation =
                    "Could not identify the hash function of the hybrid algorithm",
                node = this,
                operator = GenericQueryOperators.EVALUATE,
            )

    // One of the two things is redundant
    val keyexchangeOk =
        this.keyExchange?.let { keyExchange -> with(keyExchange) { catalog.checkKeyExchange() } }
            ?: QueryTree(
                value = false,
                stringRepresentation =
                    "Could not identify the key exchange mechanism of the hybrid algorithm",
                node = this,
                operator = GenericQueryOperators.EVALUATE,
            )

    val keysizeOk =
        this.keySize?.let {
            it
            QueryTree(
                value = it >= 3000,
                stringRepresentation =
                    if (it >= 3000) "The keysize is bigger than 3000 bit"
                    else "The keysize $it is smaller than the required 3000 bit",
                node = this,
                operator = GenericQueryOperators.EVALUATE,
            )
        }
            ?: QueryTree(
                value = false,
                stringRepresentation = "Could not identify the keysize",
                node = this,
                operator = GenericQueryOperators.EVALUATE,
            )

    return listOf(isDLIES, keysizeOk, symmetricCipherOk, hashFunctionOk, keyexchangeOk)
        .mergeWithAll()
}

context(catalog: GermanCatalog)
internal fun Cipher.checkECIES(): QueryTree<Boolean> {
    if (this !is HybridCipher) {
        return QueryTree(
            value = false,
            stringRepresentation = "The algorithm is not a hybrid cipher, so it cannot be ECIES",
            node = this,
            operator = GenericQueryOperators.EVALUATE,
        )
    }

    val isECIES =
        QueryTree(
            value = this.cipherName == "ECIES",
            stringRepresentation =
                if (cipherName == "ECIES") "The algorithm is ECIES"
                else "The algorithm is not ECIES",
            node = this,
            operator = GenericQueryOperators.EVALUATE,
        )

    val symmetricCipherOk =
        this.symmetricCipher?.let { symmetricCipher ->
            with(symmetricCipher) { catalog.checkSymmetricEncryption() }
        }
            ?: QueryTree(
                value = false,
                stringRepresentation =
                    "Could not identify the symmetric cipher with the hybrid algorithm",
                node = this,
                operator = GenericQueryOperators.EVALUATE,
            )

    val hashFunctionOk =
        this.hashFunction?.let { hashFunction ->
            with(hashFunction) { catalog.checkHashFunction() }
        }
            ?: QueryTree(
                value = false,
                stringRepresentation =
                    "Could not identify the hash function of the hybrid algorithm",
                node = this,
                operator = GenericQueryOperators.EVALUATE,
            )

    // One of the two things is redundant
    val keyexchangeOk =
        this.keyExchange?.let { keyExchange -> with(keyExchange) { catalog.checkKeyExchange() } }
            ?: QueryTree(
                value = false,
                stringRepresentation =
                    "Could not identify the key exchange mechanism of the hybrid algorithm",
                node = this,
                operator = GenericQueryOperators.EVALUATE,
            )

    val keysizeOk =
        this.keySize?.let {
            it
            QueryTree(
                value = it >= 250,
                stringRepresentation =
                    if (it >= 250) "The keysize is bigger than 250 bit"
                    else "The keysize $it is smaller than the required 250 bit",
                node = this,
                operator = GenericQueryOperators.EVALUATE,
            )
        }
            ?: QueryTree(
                value = false,
                stringRepresentation = "Could not identify the keysize",
                node = this,
                operator = GenericQueryOperators.EVALUATE,
            )

    return listOf(isECIES, keysizeOk, symmetricCipherOk, hashFunctionOk, keyexchangeOk)
        .mergeWithAll()
}
