/*
 * This file is part of the Confirmate project.
 */
package de.fraunhofer.aisec.confirmate.queries

import de.fraunhofer.aisec.cpg.graph.Backward
import de.fraunhofer.aisec.cpg.graph.ContextSensitive
import de.fraunhofer.aisec.cpg.graph.FieldSensitive
import de.fraunhofer.aisec.cpg.graph.GraphToFollow
import de.fraunhofer.aisec.cpg.graph.Interprocedural
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.concepts.Concept
import de.fraunhofer.aisec.cpg.graph.concepts.Operation
import de.fraunhofer.aisec.cpg.graph.concepts.crypto.encryption.Cipher
import de.fraunhofer.aisec.cpg.query.GenericQueryOperators
import de.fraunhofer.aisec.cpg.query.Must
import de.fraunhofer.aisec.cpg.query.QueryTree
import de.fraunhofer.aisec.cpg.query.dataFlow
import de.fraunhofer.aisec.cpg.query.mergeWithAny
import kotlin.collections.contains

/*
 * This file defines queries for analyzing cryptographic practices in code using the CPG (Code Property Graph) framework.
 * As a reference, we use the BSI TR021-02 standard (https://www.bsi.bund.de/SharedDocs/Downloads/DE/BSI/Publikationen/TechnischeRichtlinien/TR02102/TR02102.html).
 */

// Symmetric encryption algorithms, their modus, and recommended key lengths. There are also
// requirements on the IV, and interdependencies between accepted key lengths and modus.
fun Cipher.checkAES(): QueryTree<Boolean> {
    return listOf(::checkAesCCM, ::checkAesGCM, ::checkAesGcmSiv, ::checkAesCBC, ::checkAesCTR)
        .map { it.invoke() }
        .mergeWithAny(this)
}

fun Cipher.isAES(): QueryTree<Boolean> {
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

fun Cipher.isModus(modus: String): QueryTree<Boolean> {
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

fun Cipher.keyIsBlockSize(): QueryTree<Boolean> {
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

fun Cipher.validKeyLength(keySizes: Collection<Int>): QueryTree<Boolean> {
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

fun Cipher.validAuthTagSize(minimalAuthTagSize: Int): QueryTree<Boolean> {
    val tagSizeAccepted = this.tagSize >= minimalAuthTagSize
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

fun Cipher.validIvSize(minimalIvSize: Int): QueryTree<Boolean> {
    val ivSizeAccepted = this.ivSize >= minimalIvSize
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

fun Cipher.ivSizeEqualsBlockSize(): QueryTree<Boolean> {
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

fun Cipher.isUniqueIv(): QueryTree<Boolean> {
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

fun Cipher.isRandomIv(): QueryTree<Boolean> {
    return dataFlow(
        this.iv,
        direction = Backward(GraphToFollow.DFG),
        type = Must,
        sensitivities = FieldSensitive + ContextSensitive,
        scope = Interprocedural(),
        predicate = { it is RngGet },
    )
}

class RNG(underlyingNode: Node?) : Concept(underlyingNode)

class RngGet(underlyingNode: Node?, val rng: RNG) : Operation(underlyingNode, rng)

fun Cipher.checkAesCCM(): QueryTree<Boolean> {
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

fun Cipher.checkAesGCM(): QueryTree<Boolean> {
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

fun Cipher.checkAesGcmSiv(): QueryTree<Boolean> {
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

fun Cipher.checkAesCBC(): QueryTree<Boolean> {
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

fun Cipher.checkAesCTR(): QueryTree<Boolean> {
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
