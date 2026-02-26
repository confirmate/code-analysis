/*
 * This file is part of the Confirmate project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package example.tagging

import de.fraunhofer.aisec.cpg.graph.Name
import de.fraunhofer.aisec.cpg.graph.codeAndLocationFrom
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.*
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberAccess
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCall
import de.fraunhofer.aisec.cpg.passes.concepts.TaggingContext
import de.fraunhofer.aisec.cpg.passes.concepts.each
import de.fraunhofer.aisec.cpg.passes.concepts.with

/**
 * Tagging for Python sqlalchemy write operations:
 * - db.session.add()
 * - db.session.commit()
 */
fun TaggingContext.tagDatabaseWrite() {
    // db.session.add()
    each<MemberCall>(
            predicate = {
                it.name.localName == "add" &&
                    (it.callee as? MemberAccess)?.base?.name?.localName == "session"
            }
        )
        .with {
            val dbStorage = getOrCreateDatabaseStorage(node)
            createDatabaseQuery(node, dbStorage, isModify = true)
        }

    // db.session.commit()
    each<MemberCall>(
            predicate = {
                it.name.localName == "commit" &&
                    (it.callee as? MemberAccess)?.base?.name?.localName == "session"
            }
        )
        .with {
            val dbStorage = getOrCreateDatabaseStorage(node)
            createDatabaseQuery(node, dbStorage, isModify = true)
        }
}

/**
 * Tagging for python sqlalchemy read operations:
 * - Model.query.filter_by()
 * - Model.query.filter()
 * - Model.query.get()
 * - Model.query.all()
 * - Model.query.first()
 */
fun TaggingContext.tagDatabaseRead() {
    each<MemberCall>("*.query.filter_by").with {
        val dbStorage = getOrCreateDatabaseStorage(node)
        createDatabaseQuery(node, dbStorage, isModify = false)
    }

    each<MemberCall>("*.query.filter").with {
        val dbStorage = getOrCreateDatabaseStorage(node)
        createDatabaseQuery(node, dbStorage, isModify = false)
    }

    each<MemberCall>("*.query.get").with {
        val dbStorage = getOrCreateDatabaseStorage(node)
        createDatabaseQuery(node, dbStorage, isModify = false)
    }

    each<MemberCall>("*.query.all").with {
        val dbStorage = getOrCreateDatabaseStorage(node)
        createDatabaseQuery(node, dbStorage, isModify = false)
    }

    each<MemberCall>("*.query.first").with {
        val dbStorage = getOrCreateDatabaseStorage(node)
        createDatabaseQuery(node, dbStorage, isModify = false)
    }
}

/** Shared [DatabaseStorage] concept * */
private var databaseStorage: DatabaseStorage? = null

/** Get or create a [DatabaseStorage] concept */
private fun getOrCreateDatabaseStorage(node: MemberCall): DatabaseStorage {
    return databaseStorage
        ?: DatabaseStorage(
                timeToLiveSeconds = null,
                hasAutomatedCleanup = false,
                activityLogging = null,
                atRestEncryption = null,
                backups = mutableListOf(),
                immutability = null,
                resourceLogging = null,
                internetAccessibleEndpoint = null,
                geoLocation = null,
                loggings = mutableListOf(),
                redundancies = null,
                usageStatistics = null,
                creation_time = null,
                description = "Database",
                resourceId = "",
                labels = null,
                name = "database",
                raw = null,
                parent = null,
                underlyingNode = node,
            )
            .apply {
                this.codeAndLocationFrom(node)
                databaseStorage = this
            }
}

/** Helper to create a [DatabaseQuery] operation */
private fun createDatabaseQuery(
    node: MemberCall,
    storage: DatabaseStorage,
    isModify: Boolean,
): DatabaseQuery {
    return DatabaseQuery(
            parameters = node.arguments,
            modify = isModify,
            calls = null,
            databaseService = null,
            storage = storage,
            linkedConcept = storage,
        )
        .apply {
            this.codeAndLocationFrom(node)
            this.name = Name(node.name.localName)
        }
}

fun TaggingContext.tagPythonDatabase() {
    tagDatabaseWrite()
    tagDatabaseRead()
}
