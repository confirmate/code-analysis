/*
 * This file is part of the Confirmate project.
 */
package de.fraunhofer.aisec.confirmate.integration

import de.fraunhofer.aisec.codyze.AnalysisResult
import de.fraunhofer.aisec.confirmate.generated.orchestrator.model.AssessmentResult

fun AnalysisResult.toAssessmentResult(): AssessmentResult {
    return AssessmentResult(
        id = TODO(),
        createdAt = TODO(),
        metricId = TODO(),
        metricConfiguration = TODO(),
        evidenceId = TODO(),
        resourceId = TODO(),
        resourceTypes = TODO(),
        complianceComment = TODO(),
        targetOfEvaluationId = TODO(),
        toolId = TODO(),
        historyUpdatedAt = TODO(),
        history = TODO(),
        compliant = TODO(),
        complianceDetails = TODO(),
    )
}
