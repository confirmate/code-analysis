/*
 * This file is part of the Confirmate project.
 */
package de.fraunhofer.aisec.confirmate.integration

import de.fraunhofer.aisec.codyze.AnalysisResult
import io.clouditor.model.AssessmentResult
import io.clouditor.model.Evidence
import io.clouditor.model.MetricConfiguration
import java.time.OffsetDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

const val codyzeToolId = "Codyze"

@OptIn(ExperimentalUuidApi::class)
fun AnalysisResult.toClouditorResults(): Pair<List<AssessmentResult>, List<Evidence>> {
    val currentTimestamp = OffsetDateTime.now() // TODO: Should this be the timestamp when we started the analysis?

    val toeId = this.translationResult.id.toString()

    val evidences = mutableListOf<Evidence>()

    val assessmentResults = this.requirementsResults.map { (requirementId, result) ->
        evidences.add(
            Evidence(
                id = result.id.toString(),
                timestamp = currentTimestamp,
                cloudServiceId = null,
                toolId = codyzeToolId,
                raw = result.printNicely(),
                resource = null,
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
            evidenceId = result.id.toString(),
            resourceId = "TODO",
            resourceTypes = listOf(),
            complianceComment = result.printNicely(),
            targetOfEvaluationId = toeId,
            toolId = codyzeToolId,
            historyUpdatedAt = currentTimestamp,
            history = listOf(),
            complianceDetails = listOf(),
        )
    }
    return assessmentResults to evidences
}
