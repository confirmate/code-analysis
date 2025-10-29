package org.example.passes

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Name
import de.fraunhofer.aisec.cpg.graph.namespaces
import de.fraunhofer.aisec.cpg.passes.SymbolResolver
import de.fraunhofer.aisec.cpg.passes.TranslationResultPass
import de.fraunhofer.aisec.cpg.passes.configuration.DependsOn
import io.clouditor.model.Application
import io.clouditor.model.CodeModule
import io.clouditor.model.CodeRepository
import io.clouditor.model.Library

@DependsOn(SymbolResolver::class)
class ModulePass(ctx: TranslationContext)  : TranslationResultPass(ctx) {

    override fun accept(t: TranslationResult) {
        var repo = CodeRepository(
            id = "cra-demo",
            name = "cra-demo",
            description = "CRA Demo Repository"
        )

        var app = Application(
            id = "pythonpass",
            name = "pythonpass",
            codeRepositoryId = repo.id,
            functionalities = mutableListOf(),
            libraryIds = listOf("Flask-Cors", "Flask-SQLAlchemy"),
            parentId = "/subscriptions/196946e1-7029-4d97-8f65-ba6c6138c93f/resourcegroups/passwordmanagerservice",
        )

        var flask = Library(
            id = "Flask",
            name = "Flask==3.0.3",
        )

        // Static for the demo
        var flaskCors = Library(
            id ="Flask-Cors",
            name = "Flask-Cors==5.0.0",
            libraryIds = listOf("Flask"),
        )

        var flaskSQLAlchemy = Library(
            id = "Flask-SQLAlchemy",
            name = "Flask-SQLAlchemy==3.1.1",
            libraryIds = listOf("Flask", "SQLAlchemy"),
        )

        var sqlAlchemy = Library(
            id = "SQLAlchemy",
            name = "SQLAlchemy==2.0.36",
        )
        /*
Flask-Cors==5.0.0
  Flask==3.0.3
    blinker==1.8.2
    click==8.1.7
    itsdangerous==2.2.0
    Jinja2==3.1.4
      MarkupSafe==3.0.2
    Werkzeug==3.0.4
      MarkupSafe==3.0.2
Flask-SQLAlchemy==3.1.1
  Flask==3.0.3
    blinker==1.8.2
    click==8.1.7
    itsdangerous==2.2.0
    Jinja2==3.1.4
      MarkupSafe==3.0.2
    Werkzeug==3.0.4
      MarkupSafe==3.0.2
  SQLAlchemy==2.0.36
    typing_extensions==4.12.2
         */

        // Filter the root namespaces
        var modules = t.namespaces.filter { it.namespaces.size == 1 }.map {
            val parent = it.name.parent
            CodeModule(
                id = it.name.toString(),
                name = it.name.localName,
                parentId = if(parent != null) {
                    parent.toString()
                } else {
                    app.id
                },
                functionalities = mutableListOf()
            )
        }

        t.addOntologyObjects(
            repo,
            app,
            flask, flaskCors, flaskSQLAlchemy, sqlAlchemy,
            *modules.toTypedArray(),
            )

    }

    override fun cleanup() {
        // Nothing to do
    }

}

private fun TranslationResult.addOntologyObjects(
    vararg obj: Any,
) {
    // We re-use the scratch field behind the scenes
    this.ontologyObjects.addAll(obj)
}

typealias OntologyObjects = MutableList<Any>

val TranslationResult.ontologyObjects: OntologyObjects
    get() {
        return this.scratch.computeIfAbsent("ontology") {
            mutableListOf<Any>()
        } as MutableList<Any>
    }

fun OntologyObjects.findCodeModuleByName(name: Name): CodeModule? {
    return filterIsInstance<CodeModule>().firstOrNull { true && it.id == name.toString() }
}
