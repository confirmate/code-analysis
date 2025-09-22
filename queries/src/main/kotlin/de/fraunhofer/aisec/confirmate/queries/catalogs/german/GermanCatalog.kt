/*
 * This file is part of the Confirmate project.
 */
package de.fraunhofer.aisec.confirmate.queries.catalogs.german

import de.fraunhofer.aisec.confirmate.queries.SymmetricCipher
import de.fraunhofer.aisec.confirmate.queries.catalogs.CryptoCatalog
import de.fraunhofer.aisec.confirmate.queries.catalogs.RequirementsCatalog
import de.fraunhofer.aisec.cpg.graph.concepts.crypto.encryption.Cipher
import de.fraunhofer.aisec.cpg.query.QueryTree
import de.fraunhofer.aisec.cpg.query.mergeWithAny
import de.fraunhofer.aisec.cpg.query.or

class GermanCatalog : RequirementsCatalog(), CryptoCatalog {
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

    override fun checkPQCEncryption(): QueryTree<Boolean> {
        TODO("Not yet implemented")
    }
}
