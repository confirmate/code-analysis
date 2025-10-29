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
package de.fraunhofer.aisec.confirmate.integration

import de.fraunhofer.aisec.codyze.AnalysisResult
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Component
import de.fraunhofer.aisec.cpg.graph.firstParentOrNull
import de.fraunhofer.aisec.cpg.query.QueryTree
import io.clouditor.model.AssessmentResult
import io.clouditor.model.Evidence
import io.clouditor.model.MetricConfiguration
import io.clouditor.model.ObjectStorage
import io.clouditor.model.Resource
import java.time.OffsetDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
                resourceId =
                    "http://localhost:8080/components/${this.node?.firstParentOrNull<Component>()?.name ?: ""}",
                resourceTypes = listOf("Code"),
                complianceComment =
                    """
                    ${this.stringRepresentation}
                  
                    [View the result in Codyze](http://localhost:8080/requirements/$requirementId?targetNodeId=${this.id})
                    """
                        .trimIndent(),
                targetOfEvaluationId = toeId,
                toolId = codyzeToolId,
                historyUpdatedAt = currentTimestamp,
                history = listOf(io.clouditor.model.Record(this.id.toString(), currentTimestamp)),
                complianceDetails = listOf(),
            )
        )
    } else {
        // No metric ID, so we cannot create an assessment result. Go to the children
        return this.children.flatMap { it.toAssessmentResult(requirementId, evidences) }
    }
}

@OptIn(ExperimentalUuidApi::class)
fun AnalysisResult.toClouditorResults(): Pair<Set<AssessmentResult>, Set<Evidence>> {
    val currentTimestamp =
        OffsetDateTime.now() // TODO: Should this be the timestamp when we started the analysis?

    val evidences = mutableSetOf<Evidence>()
    val assessmentResults = mutableSetOf<AssessmentResult>()

    with(currentTimestamp) {
        with(this@toClouditorResults.translationResult) {
            this@toClouditorResults.requirementsResults.forEach { (requirementId, result) ->
                assessmentResults.addAll(result.toAssessmentResult(requirementId, evidences))
            }
        }
    }
    return assessmentResults to evidences
}
