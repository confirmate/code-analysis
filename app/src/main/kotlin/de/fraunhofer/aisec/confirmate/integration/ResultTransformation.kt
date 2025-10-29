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
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.*
import de.fraunhofer.aisec.cpg.query.QueryTree
import io.clouditor.model.*
import java.time.OffsetDateTime
import kotlin.uuid.*

const val codyzeToolId = "Codyze"

context(currentTimestamp: OffsetDateTime, toe: TranslationResult)
@OptIn(ExperimentalUuidApi::class)
private fun QueryTree<*>.toAssessmentResult(
    requirementId: String,
    evidences: MutableSet<Evidence>,
): List<AssessmentResult> {
    val metricId = this.metricId
    val value = this.value as? Boolean
    val toeId = toe.id.toString()
    if (value != null && metricId != null) {
        // The clouditor needs a new UUID for the evidence, so we generate one here
        // and reference it in the assessment result.
        val evidenceId = Uuid.random().toString()
        val evidence =
            Evidence(
                id = evidenceId,
                timestamp = currentTimestamp,
                targetOfEvaluationId = toeId,
                toolId = codyzeToolId,
                resource =
                    Resource(
                        objectStorage =
                            ObjectStorage(
                                id = "manual resource: object storage 0",
                                name = "object storage 0",
                            )
                    ),
            )
        evidences.add(evidence)

        // We have a metric ID, so we can create an assessment result
        return listOf(
            AssessmentResult(
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
                resourceId = this.node?.firstParentOrNull<Component>()?.id.toString(),
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
        )
    } else {
        // No metric ID, so we cannot create an assessment result. Go to the children
        return this.children.flatMap { it.toAssessmentResult(requirementId, evidences) }
    }
}

data class ConfirmateResults(
    val assessmentResult: Set<AssessmentResult>,
    val evidences: List<Evidence>,
)

@OptIn(ExperimentalUuidApi::class)
fun AnalysisResult.toConfirmateResult(): ConfirmateResults {
    val currentTimestamp =
        OffsetDateTime.now() // TODO: Should this be the timestamp when we started the analysis?

    val evidences = mutableSetOf<Evidence>()
    val assessmentResults = mutableSetOf<AssessmentResult>()

    with(currentTimestamp) {
        with(this@toConfirmateResult.translationResult) {
            this@toConfirmateResult.requirementsResults.forEach { (requirementId, result) ->
                assessmentResults.addAll(result.toAssessmentResult(requirementId, evidences))
            }
        }
    }

    return ConfirmateResults(
        assessmentResults,
        this.translationResult.components.map { with(this.translationResult) { it.toEvidence() } },
    )
}

/** Converts a [Component] to an [Evidence] object for Confirmate. */
context(toe: TranslationResult)
private fun Component.toEvidence(): Evidence {
    return Evidence(
        id = this.id.toString(),
        timestamp = OffsetDateTime.now(),
        targetOfEvaluationId = toe.id.toString(),
        toolId = codyzeToolId,
        resource = this.toResource(),
    )
}

context(toe: TranslationResult)
@OptIn(ExperimentalUuidApi::class)
private fun Component.toResource(): Resource {
    // Check, if this is an application or library
    val isLibrary = this.name.contains("Library") || this.name.startsWith("lib")

    return if (isLibrary) {
        Resource(
            library = Library(id = this.id.toString(), name = this.name.toString(), raw = this.code)
        )
    } else {
        Resource(
            application =
                Application(id = this.id.toString(), name = this.name.toString(), raw = this.code)
        )
    }
}
