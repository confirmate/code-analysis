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
@file:OptIn(ExperimentalUuidApi::class)

package de.fraunhofer.aisec.confirmate.integration

import de.fraunhofer.aisec.codyze.AnalysisResult
import de.fraunhofer.aisec.confirmate.codyzePort
import de.fraunhofer.aisec.confirmate.passes.ontologyObjects
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.*
import de.fraunhofer.aisec.cpg.graph.scopes.NamespaceScope
import de.fraunhofer.aisec.cpg.query.QueryTree
import io.clouditor.model.*
import io.ktor.util.logging.Logger
import java.time.OffsetDateTime
import kotlin.uuid.*

const val codyzeToolId = "Codyze"

val mapResourceToId = mutableMapOf<Node, String>()

val Node.resourceId: String?
    get() {
        var node =
            if (this is OverlayNode) {
                this.underlyingNode
            } else {
                this
            }
        do {
            mapResourceToId[node]?.let {
                return it
            }
            node = node?.astParent
        } while (node != null)

        return null
    }

private fun QueryTree<*>.createAssessmentResult(
    requirementId: String,
    resourceId: String,
    value: Boolean,
    metricId: String,
    currentTimestamp: OffsetDateTime,
    toeId: String,
    evidenceId: String,
): AssessmentResult {
    return AssessmentResult(
        id = Uuid.random().toString(),
        createdAt = currentTimestamp,
        metricId = metricId,
        metricConfiguration =
            MetricConfiguration(
                operator = "==",
                targetValue = true,
                metricId = metricId,
                targetOfEvaluationId = toeId,
                isDefault = true,
            ),
        compliant = value,
        evidenceId = evidenceId,
        resourceId = resourceId,
        resourceTypes = listOf("Code"),
        complianceComment =
            """
                    ${this.stringRepresentation}
                  
                    [View the result in Codyze](http://localhost:$codyzePort/requirements/$requirementId?targetNodeId=${this.id})
                    """
                .trimIndent(),
        targetOfEvaluationId = toeId,
        toolId = codyzeToolId,
        historyUpdatedAt = currentTimestamp,
        history = listOf(Record(this.id.toString(), currentTimestamp)),
        complianceDetails = listOf(),
    )
}

fun QueryTree<*>.flattenChildren(originalQt: QueryTree<*>): List<QueryTree<*>> {
    return if (this.node?.resourceId != null) {
        listOf(this)
    } else if (children.isNotEmpty()) {
        this.children.flatMap { it.flattenChildren(originalQt) }
    } else {
        listOf(originalQt)
    }
}

context(currentTimestamp: OffsetDateTime, toe: TranslationResult)
@OptIn(ExperimentalUuidApi::class)
private fun QueryTree<*>.toAssessmentResult(
    requirementId: String,
    evidenceId: String,
): List<AssessmentResult> {
    val metricId = this.metricId
    val value = this.value as? Boolean
    val toeId = "00000000-0000-0000-0000-000000000000" // toe.id.toString()
    if (value != null && metricId != null) {
        // We have a metric ID, so we can create an assessment result
        // Go down a bit further until there is a resource ID for the children.
        val flattenedQueryTrees = this.flattenChildren(this).toSet()

        return flattenedQueryTrees.map { qt ->
            val perciseValue = qt.value as? Boolean ?: value
            createAssessmentResult(
                requirementId = requirementId,
                resourceId = qt.node?.resourceId.toString(),
                value = perciseValue || value,
                metricId = metricId,
                currentTimestamp = currentTimestamp,
                toeId = toeId,
                evidenceId = evidenceId,
            )
        }
    } else {
        // No metric ID, so we cannot create an assessment result. Go to the children
        return this.children.flatMap { it.toAssessmentResult(requirementId, evidenceId) }
    }
}

data class ConfirmateResults(
    val assessmentResult: Set<AssessmentResult>,
    val evidences: MutableList<Evidence>,
)

typealias ResourceId = String

context(log: Logger)
@OptIn(ExperimentalUuidApi::class)
fun AnalysisResult.toConfirmateResult(): ConfirmateResults {
    val currentTimestamp =
        OffsetDateTime.now() // TODO: Should this be the timestamp when we started the analysis?

    val assessmentResults = mutableSetOf<AssessmentResult>()
    val evidences = mutableListOf<Evidence>()

    // Get the first component, that will be our "main" evidence ID
    var evidenceId = ""

    // Loop through all components
    with(this.translationResult) {
        for (component in components) {
            log.info("Creating evidence for component ${component.name}")

            val app =
                with(this.translationResult) {
                    component.toResource().toEvidence()
                } // Create evidence for component
            if (evidenceId.isBlank()) {
                evidenceId = app.id.toString()
            }
            evidences += app

            val modules =
                component.namespaces
                    .mapNotNull { it.declaringScope }
                    .filterIsInstance<NamespaceScope>()
                    .toSet()
                    .map {
                        log.info("Creating evidence for namespace ${it.name}")
                        it.toResource(component.id.toString()).toEvidence()
                    }

            evidences += modules
        }

        // Add library resources from ontologyObjects (e.g., from RequirementsPass)
        val libraryResources =
            this@toConfirmateResult.translationResult.ontologyObjects.filterIsInstance<Library>()
        libraryResources.forEach { library ->
            log.info("Creating evidence for library ${library.name}")
            val libraryEvidence =
                with(this@toConfirmateResult.translationResult) {
                    Resource(library = library).toEvidence()
                }
            evidences += libraryEvidence
        }
    }

    with(currentTimestamp) {
        with(this@toConfirmateResult.translationResult) {
            this@toConfirmateResult.requirementsResults.forEach { (requirementId, result) ->
                assessmentResults.addAll(result.toAssessmentResult(requirementId, evidenceId))
            }
        }
    }

    return ConfirmateResults(assessmentResults, evidences)
}

/**
 * Converts a [Resource] to an [Evidence] object for Confirmate.
 *
 * This will create a new evidence (with a new ID), but the resource ID will stay the same.
 */
context(toe: TranslationResult)
private fun Resource.toEvidence(): Evidence {
    return Evidence(
        // new evidence ID
        id = Uuid.random().toString(),
        timestamp = OffsetDateTime.now(),
        targetOfEvaluationId = "00000000-0000-0000-0000-000000000000", // toe.id.toString()
        toolId = codyzeToolId,
        resource = this,
    )
}

/**
 * Converts a [Component] to a [Resource] object for Confirmate.
 *
 * This will have the same ID as the component.
 */
@OptIn(ExperimentalUuidApi::class)
private fun Component.toResource(): Resource {
    // Check, if this is an application or library
    val isLibrary = this.name.contains("Library") || this.name.startsWith("lib")

    return if (isLibrary) {
        mapResourceToId[this] = this.id.toString()
        Resource(
            library = Library(id = this.id.toString(), name = this.name.toString(), raw = this.code)
        )
    } else {
        mapResourceToId[this] = this.id.toString()
        Resource(
            application =
                Application(id = this.id.toString(), name = this.name.toString(), raw = this.code)
        )
    }
}

/** Converts a [NamespaceScope] to a [Resource] object for Confirmate. */
fun NamespaceScope.toResource(componentId: String? = null): Resource {
    val parent = this.name.parent
    mapResourceToId[this] = this.name.toString()
    return Resource(
        sourceCodeFile =
            SourceCodeFile(
                id = this.name.toString(),
                name = this.name.localName,
                parentId =
                    if (parent != null) {
                        parent.toString()
                    } else {
                        componentId
                    },
                functionalities = mutableListOf(),
            )
    )
}
