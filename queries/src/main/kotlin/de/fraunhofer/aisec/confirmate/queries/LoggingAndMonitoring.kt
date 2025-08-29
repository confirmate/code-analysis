/*
 * This file is part of the Confirmate project.
 */
package de.fraunhofer.aisec.confirmate.queries

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Backward
import de.fraunhofer.aisec.cpg.graph.GraphToFollow
import de.fraunhofer.aisec.cpg.graph.Interprocedural
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.concepts.logging.LogWrite
import de.fraunhofer.aisec.cpg.query.Must
import de.fraunhofer.aisec.cpg.query.QueryTree
import de.fraunhofer.aisec.cpg.query.allExtended
import de.fraunhofer.aisec.cpg.query.executionPath

context(translationResult: TranslationResult)
fun relevantActivityHasLogging(
    relevantActivitiesSpecifier: ((Node) -> Boolean)
): QueryTree<Boolean> {
    return translationResult.allExtended<Node>(relevantActivitiesSpecifier) { relevantActivity ->
        executionPath(
            startNode = relevantActivity,
            direction = Backward(GraphToFollow.EOG),
            type = Must,
            scope = Interprocedural(),
            predicate = { node -> node is LogWrite && node.isLevelEnabled },
        )
    }
}

context(translationResult: TranslationResult)
fun relevantActivityHasLoggingWithMeaningfulMessage(
    relevantActivitiesSpecifier: ((Node) -> Boolean)
): QueryTree<Boolean> {
    return translationResult.allExtended<Node>(relevantActivitiesSpecifier) { relevantActivity ->
        executionPath(
            startNode = relevantActivity,
            direction = Backward(GraphToFollow.EOG),
            type = Must,
            scope = Interprocedural(),
            predicate = { node ->
                node is LogWrite &&
                    node.isLevelEnabled &&
                    messageMatchesActivity(node.logArguments, relevantActivity)
            },
        )
    }
}

val LogWrite.isLevelEnabled: Boolean
    get() {
        // TODO: Implement proper log level threshold checking. We need to know what is configured
        // for the respective logger.
        //   There are several ways to achieve this, e.g., by looking at the configuration files or
        // by tracking
        //   logger initialization in the code. We should probably add a field to the Concept and
        // add a check like:
        //   return this.logLevel >= this.concept.logLevelThreshold
        return true
    }

fun messageMatchesActivity(arguments: List<Node>, relevantActivity: Node): Boolean {
    // TODO: Maybe use an AI agent to decide if the message constructed by the arguments is
    // meaningful for the activity.
    return true
}
