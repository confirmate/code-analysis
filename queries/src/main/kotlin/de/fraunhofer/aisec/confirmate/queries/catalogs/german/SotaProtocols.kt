/*
 * This file is part of the Confirmate project.
 */
package de.fraunhofer.aisec.confirmate.queries.catalogs.german

import de.fraunhofer.aisec.confirmate.queries.CommunicationProtocol
import de.fraunhofer.aisec.confirmate.queries.TLS1_2_CipherSuite
import de.fraunhofer.aisec.confirmate.queries.TLS1_3
import de.fraunhofer.aisec.cpg.query.GenericQueryOperators
import de.fraunhofer.aisec.cpg.query.QueryTree
import de.fraunhofer.aisec.cpg.query.and
import de.fraunhofer.aisec.cpg.query.mergeWithAll

fun CommunicationProtocol.isProtocol(name: String, version: String): QueryTree<Boolean> {
    val correct = this.protocolName == name && this.versionNumber == version
    return QueryTree(
        value = correct,
        stringRepresentation =
            if (correct) "Protocol is $name version $version"
            else
                "Protocol is $protocolName version $versionNumber, expected $name version $version",
        node = this,
        operator = GenericQueryOperators.EVALUATE,
    )
}

/**
 * Checks whether the protocol is TLS 1.2 with recommended ciphers and settings. [requiresPFS] is
 * set to `true` if perfect forward secrecy is required by the use-case. [forbidsPSK] should be set
 * to `true` if the use-case does not allow for a pre-shared key. Setting these parameters correctly
 * is important as they influence which ciphers are considered secure.
 */
internal fun CommunicationProtocol.checkTLS1_2(
    requiresPFS: Boolean = false,
    forbidsPSK: Boolean = false,
): QueryTree<Boolean> {
    val isTLS12 = isProtocol("TLS", "1.2")
    val goodCiphersWithPFS =
        setOf(
            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_ECDSA_WITH_AES_128_CCM",
            "TLS_ECDHE_ECDSA_WITH_AES_256_CCM",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_DHE_DSS_WITH_AES_128_CBC_SHA256",
            "TLS_DHE_DSS_WITH_AES_256_CBC_SHA256",
            "TLS_DHE_DSS_WITH_AES_128_GCM_SHA256",
            "TLS_DHE_DSS_WITH_AES_256_GCM_SHA384",
            "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256",
            "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_DHE_RSA_WITH_AES_128_CCM",
            "TLS_DHE_RSA_WITH_AES_256_CCM",
        )
    val goodCiphersWithoutPFS =
        setOf(
            "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_DH_DSS_WITH_AES_128_CBC_SHA256",
            "TLS_DH_DSS_WITH_AES_256_CBC_SHA256",
            "TLS_DH_DSS_WITH_AES_128_GCM_SHA256",
            "TLS_DH_DSS_WITH_AES_256_GCM_SHA384",
            "TLS_DH_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_DH_RSA_WITH_AES_256_CBC_SHA256",
            "TLS_DH_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_DH_RSA_WITH_AES_256_GCM_SHA384",
        )
    val goodCiphersPsk =
        setOf(
            "TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA256",
            "TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA384",
            "TLS_ECDHE_PSK_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_PSK_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_PSK_WITH_AES_128_CCM_SHA256",
            "TLS_DHE_PSK_WITH_AES_128_CBC_SHA256",
            "TLS_DHE_PSK_WITH_AES_256_CBC_SHA384",
            "TLS_DHE_PSK_WITH_AES_128_GCM_SHA256",
            "TLS_DHE_PSK_WITH_AES_256_GCM_SHA384",
            "TLS_DHE_PSK_WITH_AES_128_CCM",
            "TLS_DHE_PSK_WITH_AES_256_CCM",
            "TLS_RSA_PSK_WITH_AES_128_CBC_SHA256",
            "TLS_RSA_PSK_WITH_AES_256_CBC_SHA384",
            "TLS_RSA_PSK_WITH_AES_128_GCM_SHA256",
            "TLS_RSA_PSK_WITH_AES_256_GCM_SHA384",
        )

    val supportedGroups =
        setOf(
            "secp256r1",
            "secp384r1",
            "secp521r1",
            "brainpoolP256r1",
            "brainpoolP384r1",
            "brainpoolP512r1",
            "ffdhe3072",
            "ffdhe4096",
        )
    val allAllowed = goodCiphersWithPFS.toMutableSet()

    if (!requiresPFS) allAllowed.addAll(goodCiphersWithoutPFS)
    if (!forbidsPSK) allAllowed.addAll(goodCiphersPsk)

    val acceptedSuites =
        this.cipherSuites
            ?.map { cipherSuite ->
                if (cipherSuite is TLS1_2_CipherSuite) {
                    val isOk = cipherSuite.ciphersuiteName in allAllowed
                    val ciphersuiteAllowed =
                        QueryTree(
                            value = isOk,
                            stringRepresentation =
                                if (isOk) {
                                    "TLS 1.2 cipher suite ${cipherSuite.ciphersuiteName} is allowed"
                                } else {
                                    "TLS 1.2 cipher suite ${cipherSuite.ciphersuiteName} is NOT allowed"
                                },
                            node = cipherSuite,
                            operator = GenericQueryOperators.EVALUATE,
                        )
                    if (
                        cipherSuite.ciphersuiteName.startsWith("TLS_ECDHE") ||
                            cipherSuite.ciphersuiteName.startsWith("TLS_DHE")
                    ) {
                        val supportedGroupsOk =
                            cipherSuite.supportedGroups
                                .map {
                                    val groupOk = it in supportedGroups
                                    QueryTree(
                                        value = groupOk,
                                        stringRepresentation =
                                            if (groupOk) {
                                                "TLS 1.2 cipher suite ${cipherSuite.ciphersuiteName} uses a recommended supported group: $it"
                                            } else {
                                                "TLS 1.2 cipher suite ${cipherSuite.ciphersuiteName} uses a NOT recommended supported group: $it"
                                            },
                                        node = cipherSuite,
                                        operator = GenericQueryOperators.EVALUATE,
                                    )
                                }
                                .mergeWithAll()
                        ciphersuiteAllowed and supportedGroupsOk
                    } else ciphersuiteAllowed
                } else {
                    QueryTree(
                        value = false,
                        stringRepresentation =
                            "Cipher suite $cipherSuite is not a TLS 1.2 cipher suite?!",
                        node = cipherSuite,
                        operator = GenericQueryOperators.EVALUATE,
                    )
                }
            }
            ?.mergeWithAll()
            ?: QueryTree(
                value = false,
                stringRepresentation = "No cipher suites found for TLS 1.2",
                node = this,
                operator = GenericQueryOperators.EVALUATE,
            )
    // There are several other recommendations e.g. about renevotiation, extensions like
    // encrypt-then-mac, heartbeat, extended master secret. However, these aren't present in the
    // model yet, so we do not check them for now.

    return isTLS12 and acceptedSuites
}

/**
 * Checks whether the protocol is TLS 1.3 with recommended ciphers and settings. [forbidsPSK] should
 * be set to `true` if the use-case does not allow for a pre-shared key.
 */
internal fun CommunicationProtocol.checkTLS1_3(forbidsPSK: Boolean = false): QueryTree<Boolean> {
    val isTLS13 = isProtocol("TLS", "1.3")
    if (this !is TLS1_3) {
        return QueryTree(
            value = false,
            stringRepresentation = "The protocol is not TLS 1.3",
            node = this,
            operator = GenericQueryOperators.EVALUATE,
        )
    } else if (!isTLS13.value) {
        return isTLS13
    }

    val recommendedHandshakeWithPSK =
        setOf(
            "psk_ke", // Until 2026
            "psk_dhe_ke",
        )
    val usesOnlyRecommendedPSKHandshakeModes =
        this.pskHandshakeModes
            .map { mode ->
                val isRecommendedMode = mode in recommendedHandshakeWithPSK
                QueryTree(
                    value = isRecommendedMode,
                    stringRepresentation =
                        if (isRecommendedMode) {
                            "TLS 1.3 PSK handshake mode $mode is recommended"
                        } else {
                            "TLS 1.3 PSK handshake mode $mode is NOT recommended. Should be one of $recommendedHandshakeWithPSK"
                        },
                    node = this,
                    operator = GenericQueryOperators.EVALUATE,
                )
            }
            .mergeWithAll()

    val recommendedSupportedGroups =
        setOf(
            "secp256r1",
            "secp384r1",
            "secp521r1",
            "brainpoolP256r1tls13",
            "brainpoolP384r1tls13",
            "brainpoolP512r1tls13",
            "ffdhe3072",
            "ffdhe4096",
        )
    val usesOnlyRecommendedSupportedGroups =
        this.supportedGroups
            .map { group ->
                val isRecommendedGroup = group in recommendedSupportedGroups
                QueryTree(
                    value = isRecommendedGroup,
                    stringRepresentation =
                        if (isRecommendedGroup) {
                            "TLS 1.3 supported group $group is recommended"
                        } else {
                            "TLS 1.3 supported group $group is NOT recommended. Should be one of $recommendedSupportedGroups"
                        },
                    node = this,
                    operator = GenericQueryOperators.EVALUATE,
                )
            }
            .mergeWithAll()

    val recommendedSignatureAlgorithms =
        setOf(
            "rsa_pss_rsae_sha256",
            "rsa_pss_rsae_sha384",
            "rsa_pss_rsae_sha512",
            "rsa_pss_pss_sha256",
            "rsa_pss_pss_sha384",
            "rsa_pss_pss_sha512",
            "ecdsa_secp256r1_sha256",
            "ecdsa_secp384r1_sha384",
            "ecdsa_secp521r1_sha512",
            "ecdsa_brainpoolP256r1tls13_sha256",
            "ecdsa_brainpoolP384r1tls13_sha384",
            "ecdsa_brainpoolP512r1tls13_sha512",
        )
    val usesOnlyRecommendedSignatureAlgorithms =
        this.signatureAlgorithms
            .map { algorithm ->
                val isRecommendedAlg = algorithm in recommendedSignatureAlgorithms
                QueryTree(
                    value = isRecommendedAlg,
                    stringRepresentation =
                        if (isRecommendedAlg) {
                            "TLS 1.3 signature algorithm $algorithm is recommended"
                        } else {
                            "TLS 1.3 signature algorithm $algorithm is NOT recommended. Should be one of $recommendedSignatureAlgorithms"
                        },
                    node = this,
                    operator = GenericQueryOperators.EVALUATE,
                )
            }
            .mergeWithAll()

    val recommendedCertSignatureAlgorithms =
        setOf(
            "rsa_pkcs1_sha256", // Until 2025
            "rsa_pkcs1_sha384", // Until 2025
            "rsa_pkcs1_sha512", // Until 2025
            "rsa_pss_rsae_sha256",
            "rsa_pss_rsae_sha384",
            "rsa_pss_rsae_sha512",
            "rsa_pss_pss_sha256",
            "rsa_pss_pss_sha384",
            "rsa_pss_pss_sha512",
            "ecdsa_secp256r1_sha256",
            "ecdsa_secp384r1_sha384",
            "ecdsa_secp521r1_sha512",
            "ecdsa_brainpoolP256r1tls13_sha256",
            "ecdsa_brainpoolP384r1tls13_sha384",
            "ecdsa_brainpoolP512r1tls13_sha512",
        )
    val usesOnlyRecommendedCertSignatureAlgorithms =
        this.certificateSignatureAlgorithms
            ?.map { algorithm ->
                val isRecommendedAlg = algorithm in recommendedCertSignatureAlgorithms
                QueryTree(
                    value = isRecommendedAlg,
                    stringRepresentation =
                        if (isRecommendedAlg) {
                            "TLS 1.3 certificate signature algorithm $algorithm is recommended"
                        } else {
                            "TLS 1.3 certificate signature algorithm $algorithm is NOT recommended. Should be one of $recommendedCertSignatureAlgorithms"
                        },
                    node = this,
                    operator = GenericQueryOperators.EVALUATE,
                )
            }
            ?.mergeWithAll()

    val recommendedCipherSuites =
        setOf("TLS_AES_128_GCM_SHA256", "TLS_AES_256_GCM_SHA384", "TLS_AES_128_CCM_SHA256")
    val usesOnlyRecommendedCipherSuites =
        this.cipherSuites
            ?.map { suite ->
                val isRecommendedSuite = suite.name.toString() in recommendedCipherSuites
                QueryTree(
                    value = isRecommendedSuite,
                    stringRepresentation =
                        if (isRecommendedSuite) {
                            "TLS 1.3 cipher suite ${suite.name} is recommended"
                        } else {
                            "TLS 1.3 cipher suite ${suite.name} is NOT recommended. Should be one of $recommendedCipherSuites"
                        },
                    node = suite,
                    operator = GenericQueryOperators.EVALUATE,
                )
            }
            ?.mergeWithAll()
            ?: QueryTree(
                value = false,
                stringRepresentation = "No cipher suites found for TLS 1.3",
                node = this,
                operator = GenericQueryOperators.EVALUATE,
            )

    return isTLS13 and
        listOfNotNull(
                usesOnlyRecommendedPSKHandshakeModes,
                usesOnlyRecommendedSupportedGroups,
                usesOnlyRecommendedSignatureAlgorithms,
                usesOnlyRecommendedCipherSuites,
                usesOnlyRecommendedCertSignatureAlgorithms,
            )
            .mergeWithAll()
}
