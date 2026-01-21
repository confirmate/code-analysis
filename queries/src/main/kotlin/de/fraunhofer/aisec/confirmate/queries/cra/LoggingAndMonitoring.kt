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
package de.fraunhofer.aisec.confirmate.queries.cra

import de.fraunhofer.aisec.confirmate.integration.AssessesMetrics
import de.fraunhofer.aisec.confirmate.integration.RepresentsEvidences
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.assumptions.AssumptionType
import de.fraunhofer.aisec.cpg.assumptions.assume
import de.fraunhofer.aisec.cpg.graph.*
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.GetCurrentTimeOperation
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.Identity
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.LogWrite
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.Logging
import de.fraunhofer.aisec.cpg.graph.edges.flows.ControlDependence
import de.fraunhofer.aisec.cpg.query.*

context(translationResult: TranslationResult)
fun loggingEnabledByDefault(): QueryTree<Boolean> {
    return QueryTree(
            value = true,
            stringRepresentation = "Logging enabled by default",
            operator = GenericQueryOperators.EVALUATE,
        )
        .assume(
            AssumptionType.ConceptAssumption,
            """We assume that logging is enabled by default, i.e., if the user does not explicitly disable logging, it is active.
        
        To validate this assumption, we need to check the initial configuration of the logger and see that disabling logging only affects the user triggering the action."""
                .trimIndent(),
        )
}

@AssessesMetrics("SecurityDataAvailableToUser")
@RepresentsEvidences("E97")
context(translationResult: TranslationResult)
fun loggedDataAvailableToUser(
    accessToLogData: (Node) -> Boolean,
    sink: (Node) -> Boolean,
): QueryTree<Boolean> {
    return translationResult.allExtended<Node>(accessToLogData) {
        it.alwaysFlowsTo(
            identifyCopies = false,
            scope = InterproceduralWithDfgTermination(),
            predicate = sink,
        )
    }
}

/**
 * This query checks that there is an opt-out mechanism for logging. I.e., it must be possible to
 * disable logging via a specific user input. How this input is provided for the TOE is specified by
 * [inputConfiguringOptOut].
 */
context(translationResult: TranslationResult)
fun loggingOptOut(inputConfiguringOptOut: ((Node) -> Boolean)): QueryTree<Boolean> {
    // Idea: There exists a user input which flows into a CDG parent (possibly transitive) for each
    // logging statement. Probably, the usage of this opt-out input should be logged regardless of
    // its value.
    return translationResult.allExtended<LogWrite> { logWrite ->
        // We have to make use of the underlying node here, as the LogWrite itself is not connected
        // via the CDG
        val surroundingCDGNodeIsOptOut =
            logWrite.underlyingNode?.prevCDGEdges?.map { it.isOptOutCheck(inputConfiguringOptOut) }
                ?: emptyList()
        QueryTree(
                value = surroundingCDGNodeIsOptOut.any { it.value },
                children = surroundingCDGNodeIsOptOut,
                stringRepresentation = "Opt-out check for log write",
                node = logWrite,
                operator = GenericQueryOperators.EVALUATE,
            )
            .assume(
                AssumptionType.ControlFlowAssumption,
                """
                We assume that the opt-out check for logging occurs in the closest surrounding branching node.
                
                To validate this assumption, we need if there is a check further away which disables the logging or if the logging is disabled in anther way, e.g. by configuring the log level.
                Note that this is only necessary if the query fails."""
                    .trimIndent(),
            )
    }
}

fun ControlDependence.isOptOutCheck(
    inputConfiguringOptOut: ((Node) -> Boolean)
): QueryTree<Boolean> {
    val branchingNode =
        end as? BranchingNode
            ?: return QueryTree(
                value = false,
                stringRepresentation = "CDG parent is not a branching node",
                node = end,
                operator = GenericQueryOperators.EVALUATE,
            )
    val condition =
        branchingNode.branchedBy
            ?: return QueryTree(
                value = false,
                stringRepresentation = "CDG parent has no condition",
                node = end,
                operator = GenericQueryOperators.EVALUATE,
            )
    return dataFlow(
            startNode = condition,
            direction = Backward(GraphToFollow.DFG),
            type = Must,
            scope = Interprocedural(),
            predicate = inputConfiguringOptOut,
        )
        .apply {
            stringRepresentation =
                if (value)
                    "The condition of the CDG parent branches on an input configuring the logging opt-out"
                else
                    "The condition of the CDG parent does not branch on an input configuring the logging opt-out"
        }
}

/**
 * This query checks that before each relevant activity, there is a logging statement. The logging
 * statement must be triggered based on the configuration of the logger (i.e., the log level has to
 * match).
 *
 * Relevant activities must include:
 * - Modifications to settings (REQ_ER 13.1)
 * - Authentication of users (REQ_ER 13.2)
 *
 * Metric: ActivityLoggingEnabled
 */
@AssessesMetrics("ActivityLoggingEnabled")
@RepresentsEvidences("E99")
context(translationResult: TranslationResult)
fun relevantActivityHasLogging(
    relevantActivitiesSpecifier: ((Node) -> Boolean)
): QueryTree<Boolean> {
    return translationResult
        .allExtended<Node>(relevantActivitiesSpecifier) { relevantActivity ->
            executionPath(
                    startNode = relevantActivity,
                    direction = Backward(GraphToFollow.EOG),
                    type = Must,
                    scope = Interprocedural(maxSteps = 100),
                    predicate = { node -> node is LogWrite && node.isLevelEnabled },
                )
                .apply {
                    stringRepresentation =
                        if (value) "Logging statement was found before relevant activity"
                        else "No logging statement was found before relevant activity"
                }
        }
        .apply {
            stringRepresentation =
                if (value)
                    "Relevant activities have logging statements executed before them, if the log level is configured accordingly."
                else
                    "Some relevant activities do not have logging statements executed before them, even if the log level is configured accordingly."
        }
}

/**
 * This query checks that before each relevant activity, there is a logging statement. The logging
 * statement must be triggered based on the configuration of the logger (i.e., the log level has to
 * match).
 *
 * In addition, it checks that the log message is meaningful for the activity.
 */
context(translationResult: TranslationResult)
fun relevantActivityHasLoggingWithMeaningfulMessage(
    relevantActivitiesSpecifier: ((Node) -> Boolean)
): QueryTree<Boolean> {
    return translationResult
        .allExtended<Node>(relevantActivitiesSpecifier) { relevantActivity ->
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
                .apply {
                    stringRepresentation =
                        if (value)
                            "Logging statement with a meaningful message was found before relevant activity"
                        else
                            "No logging statement with a meaningful message was found before relevant activity"
                }
        }
        .apply {
            stringRepresentation =
                if (value)
                    "Relevant activities have logging statements executed before them, if the log level is configured accordingly. The logging statements have meaningful messages."
                else
                    "Some relevant activities do not have logging statements with a meaningful message executed before them, even if the log level is configured accordingly."
        }
}

/**
 * Checks whether [this] [LogWrite] would actually be executed based on the configured log level for
 * the underlying logger. The information has to be set beforehand in the [Logging] node related to
 * [this].
 */
val LogWrite.isLevelEnabled: Boolean
    get() {
        return this.logLevel != null &&
            this.linkedConcept.logLevelThreshold != null &&
            this.logLevel!! >= (this.concept as? Logging)?.logLevelThreshold!!
    }

/**
 * Checks if the message constructed by [arguments] is meaningful for the [relevantActivity]. I.e.,
 * it should reflect which activity was performed.
 */
fun messageMatchesActivity(arguments: List<Node?>, relevantActivity: Node): Boolean {
    // TODO: Maybe use an AI agent to decide if the message constructed by the arguments is
    // meaningful for the activity.
    return true
}

/** This query checks that each [LogWrite] contains a timestamp. */
context(translationResult: TranslationResult)
fun logEntriesHaveTimestamp(): QueryTree<Boolean> {
    return translationResult
        .allExtended<LogWrite> { logWrite ->
            // TODO: Add a list of fields to the Log concept which tells us what information is
            // included
            //   by each log entry. Typically, this is something like a timestamp, a logger name
            val timestampField =
                setOf<Node>() // logWrite.concept.logFields.filter{ it is LogTimestamp }
            val timestampQt =
                QueryTree(
                        value = timestampField.isNotEmpty(),
                        children =
                            listOf(
                                QueryTree(timestampField, operator = GenericQueryOperators.EVALUATE)
                            ),
                        operator = GenericQueryOperators.EVALUATE,
                    )
                    .apply {
                        stringRepresentation =
                            if (value) "All log entries have a field logging the current time"
                            else "Not all log entries have a field logging the current time"
                    }

            val argumentHasTimestamp =
                logWrite.logArguments.filterNotNull().map {
                    dataFlow(
                            startNode = it,
                            direction = Backward(GraphToFollow.DFG),
                            type = Must,
                            scope = Interprocedural(),
                            predicate = { node -> node is GetCurrentTimeOperation },
                        )
                        .apply {
                            stringRepresentation =
                                if (value) "Argument flows from a timestamp source"
                                else "Argument does not flow from a timestamp source"
                        }
                }
            val timeArgumentQt =
                QueryTree(
                        value = argumentHasTimestamp.any { it.value },
                        children = argumentHasTimestamp,
                        operator = GenericQueryOperators.EVALUATE,
                    )
                    .apply {
                        stringRepresentation =
                            if (value) "Log entry has an argument which provides a timestamp"
                            else "Log entry does not have any argument which provides a timestamp"
                    }

            (timestampQt or timeArgumentQt).apply {
                stringRepresentation =
                    if (value) "Log entry has a timestamp"
                    else "Log entry does not have a timestamp"
            }
        }
        .apply {
            stringRepresentation =
                if (value) "All log entries have a timestamp"
                else "Some log entries do not have a timestamp"
        }
}

/**
 * This query checks that each [LogWrite] has an argument which represents the initiator of the
 * action. We are currently supporting [Identity] as initiator.
 */
@AssessesMetrics("IdentityRecentActivity")
@RepresentsEvidences("E99", "E96")
context(translationResult: TranslationResult)
fun logEntriesContainInitiator(): QueryTree<Boolean> {
    return translationResult
        .allExtended<LogWrite> { logWrite ->
            logWrite.logArguments
                .filterNotNull()
                .map {
                    dataFlow(
                            startNode = it,
                            direction = Backward(GraphToFollow.DFG),
                            type = Must,
                            scope = Interprocedural(),
                            predicate = { node -> node is Identity },
                        )
                        .apply {
                            stringRepresentation =
                                if (value) "The log entry contains the initiator of the action"
                                else "The log entry does not contain the initiator of the action"
                        }
                }
                .mergeWithAny(node = logWrite)
                .assume(
                    AssumptionType.DataFlowAssumption,
                    """
                    We assume that the initiator is always logged via the same argument, i.e., the logging routine does not hold the initiator in different arguments on different paths reaching it.
                    
                    To validate this assumption, we need to check if different paths can contain the initiator and if it is enforced that at least one log argument always holds the initiator of the operation.
                    Note that this is only necessary if the query fails."""
                        .trimIndent(),
                )
                .apply {
                    stringRepresentation =
                        if (value) "The log entry contains the initiator of the action"
                        else "The log entry does not contain the initiator of the action"
                }
        }
        .apply {
            stringRepresentation =
                if (value) "All log entries contain the initiator of the action"
                else "Some log entries do not contain the initiator of the action"
        }
}
