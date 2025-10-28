package org.example.passes

import de.fraunhofer.aisec.cpg.ScopeManager
import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Name
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.scopes.NameScope
import de.fraunhofer.aisec.cpg.graph.scopes.RecordScope
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.helpers.SubgraphWalker
import de.fraunhofer.aisec.cpg.passes.EvaluationOrderGraphPass
import de.fraunhofer.aisec.cpg.passes.TranslationResultPass
import de.fraunhofer.aisec.cpg.passes.configuration.DependsOn
import de.fraunhofer.aisec.cpg.sarif.PhysicalLocation
import io.clouditor.model.*

@DependsOn(EvaluationOrderGraphPass::class)
@DependsOn(ModulePass::class)
class CryptoPass(ctx: TranslationContext) : TranslationResultPass(ctx) {
    lateinit var  result: TranslationResult

    override fun accept(t: TranslationResult) {
        result = t

        val walker = SubgraphWalker.ScopedWalker(this.scopeManager)
        walker.registerHandler(::handleNode)
        walker.iterate(result)
    }

    fun handleNode(node: Node?) {
        if(node is CallExpression && node.name.toString() =="hashlib.md5") {
            val op = CryptographicHash(
                algorithm = "md5",
                usesSalt = false,
                codeRegion = node.toCodeRegion()
            )

            val pkg = scopeManager.currentPackageScope

            val app = result.ontologyObjects.filterIsInstance<Application>().firstOrNull()
            (app?.functionalities as MutableList) += Functionality(
                cryptographicHash = op
            )

            /*val module = pkg?.let { result.ontologyObjects.findCodeModuleByName(it.name ?: Name("")) }
            (module?.functionalities as MutableList) += Functionality(
                cryptographicHash = op
            )*/
        }
    }

    override fun cleanup() {
        // Nothing to do
    }
}

private fun Node.toCodeRegion(): CodeRegion? {
    var file = this.location?.artifactLocation?.uri?.let { this.ctx?.config?.topLevel?.toURI()?.relativize(it) }

    return CodeRegion(
        file = file?.toString(),
        startLine = this.location?.region?.startLine,
        startColumn = this.location?.region?.startColumn,
        endLine = this.location?.region?.endLine,
        endColumn = this.location?.region?.endColumn
    )
}

val ScopeManager.currentPackageScope: NameScope?
    get() {
    return firstScopeOrNull(currentScope) { it is NameScope && it !is RecordScope } as NameScope?
}