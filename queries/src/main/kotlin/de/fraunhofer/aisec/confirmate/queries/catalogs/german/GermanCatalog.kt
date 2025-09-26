/*
 * This file is part of the Confirmate project.
 */
package de.fraunhofer.aisec.confirmate.queries.catalogs.german

import de.fraunhofer.aisec.confirmate.queries.CommunicationProtocol
import de.fraunhofer.aisec.confirmate.queries.SymmetricCipher
import de.fraunhofer.aisec.confirmate.queries.catalogs.CryptoCatalog
import de.fraunhofer.aisec.confirmate.queries.catalogs.RequirementsCatalog
import de.fraunhofer.aisec.confirmate.queries.catalogs.TLSCatalog
import de.fraunhofer.aisec.cpg.graph.concepts.crypto.encryption.Cipher
import de.fraunhofer.aisec.cpg.query.QueryTree
import de.fraunhofer.aisec.cpg.query.mergeWithAny
import de.fraunhofer.aisec.cpg.query.or

/**
 * Checks the following BSI TRs:
 * - [BSI TR 02102-1 version
 *   2025-01](https://www.bsi.bund.de/SharedDocs/Downloads/DE/BSI/Publikationen/TechnischeRichtlinien/TR02102/BSI-TR-02102.pdf?__blob=publicationFile&v=13)
 *   regarding cryptographic requirements
 * - [BSI TR 02102-2 version
 *   2025-01](https://www.bsi.bund.de/SharedDocs/Downloads/DE/BSI/Publikationen/TechnischeRichtlinien/TR02102/BSI-TR-02102-2.pdf?__blob=publicationFile&v=11)
 *   regarding TLS
 */
class GermanCatalog : RequirementsCatalog(), CryptoCatalog, TLSCatalog {
    context(cipher: SymmetricCipher)
    override fun checkSymmetricEncryption(): QueryTree<Boolean> {
        return cipher.checkAES()
    }

    context(cipher: Cipher)
    override fun checkAsymmetricEncryption(): QueryTree<Boolean> {
        return listOf(cipher.checkRSA(), cipher.checkDLIES(), cipher.checkECIES()).mergeWithAny()
    }

    context(cipher: Cipher)
    override fun checkKeyExchange(): QueryTree<Boolean> {
        TODO()
    }

    context(cipher: Cipher)
    override fun checkHashFunction(): QueryTree<Boolean> {
        TODO()
    }

    context(cipher: CommunicationProtocol)
    override fun checkTLS(): QueryTree<Boolean> {
        return cipher.checkTLS1_2() or cipher.checkTLS1_3()
    }

    override fun checkPQCEncryption(): QueryTree<Boolean> {
        TODO("Not yet implemented")
    }
}
