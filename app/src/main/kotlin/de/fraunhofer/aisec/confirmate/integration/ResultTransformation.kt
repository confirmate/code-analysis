/*
 * This file is part of the Confirmate project.
 */
package de.fraunhofer.aisec.confirmate.integration

import de.fraunhofer.aisec.codyze.AnalysisResult
import io.clouditor.model.AssessmentResult
import io.clouditor.model.MetricConfiguration
import java.time.OffsetDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun AnalysisResult.toAssessmentResult(): List<AssessmentResult> {
    val toeId = this.translationResult.id.toString()
    return this.requirementsResults.map { (requirementId, result) ->
        AssessmentResult(
            id = Uuid.random().toString(),
            createdAt = OffsetDateTime.now(),
            metricId = requirementId,
            metricConfiguration = MetricConfiguration(
                operator = "==",
                targetValue = true,
                metricId = requirementId,
                targetOfEvaluationId = toeId,
                isDefault = true
            ),
            compliant = result.value,
            evidenceId = result.id.toString(),
            resourceId = "TODO",
            resourceTypes = listOf(),
            complianceComment = result.printNicely(),
            targetOfEvaluationId = toeId,
            toolId = "Codyze",
            historyUpdatedAt = OffsetDateTime.now(),
            history = listOf(),
            complianceDetails = listOf(),
        )
    }
}
