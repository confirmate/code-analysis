/*
 * This file is part of the Confirmate project.
 */
package de.fraunhofer.aisec.confirmate.integration

import de.fraunhofer.aisec.codyze.AnalysisResult
import io.clouditor.model.AssessmentResult
import io.clouditor.model.Evidence
import io.clouditor.model.MetricConfiguration
import io.clouditor.model.ObjectStorage
import io.clouditor.model.Resource
import java.time.OffsetDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

const val codyzeToolId = "Codyze"

@OptIn(ExperimentalUuidApi::class)
fun AnalysisResult.toClouditorResults(): Pair<Set<AssessmentResult>, Set<Evidence>> {
    val currentTimestamp =
        OffsetDateTime.now() // TODO: Should this be the timestamp when we started the analysis?

    val toeId = this.translationResult.id.toString()

    val evidences = mutableSetOf<Evidence>()

    val assessmentResults =
        this.requirementsResults
            .map { (requirementId, result) ->
                // The clouditor needs a new UUID for the evidence, so we generate one here
                // and reference it in the assessment result.
                val evidenceId = Uuid.random().toString()
                evidences.add(
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
                )

                AssessmentResult(
                    id = Uuid.random().toString(),
                    createdAt = currentTimestamp,
                    metricId = requirementId,
                    metricConfiguration =
                        MetricConfiguration(
                            operator = "==",
                            targetValue = true,
                            metricId = requirementId,
                            targetOfEvaluationId = toeId,
                            isDefault = true,
                        ),
                    compliant = result.value,
                    evidenceId = evidenceId,
                    resourceId = "TODO",
                    resourceTypes = listOf("Code"),
                    complianceComment = result.printNicely(),
                    targetOfEvaluationId = toeId,
                    toolId = codyzeToolId,
                    historyUpdatedAt = currentTimestamp,
                    history =
                        listOf(io.clouditor.model.Record(result.id.toString(), currentTimestamp)),
                    complianceDetails = listOf(),
                )
            }
            .toSet()
    return assessmentResults to evidences
}
