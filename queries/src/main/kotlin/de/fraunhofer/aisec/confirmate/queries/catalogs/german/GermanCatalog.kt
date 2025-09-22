/*
 * This file is part of the Confirmate project.
 */
package de.fraunhofer.aisec.confirmate.queries.catalogs.german

import de.fraunhofer.aisec.confirmate.queries.catalogs.CryptoCatalog
import de.fraunhofer.aisec.confirmate.queries.catalogs.RequirementsCatalog
import de.fraunhofer.aisec.cpg.query.QueryTree

class GermanCatalog : RequirementsCatalog(), CryptoCatalog {
    context(cipher: SymmetricCipher)
    override fun checkSymmetricEncryption(): QueryTree<Boolean> {
        return cipher.checkAES()
    }

    override fun checkAsymmetricEncryption(): QueryTree<Boolean> {
        TODO("Not yet implemented")
    }

    override fun checkPQCEncryption(): QueryTree<Boolean> {
        TODO("Not yet implemented")
    }
}
