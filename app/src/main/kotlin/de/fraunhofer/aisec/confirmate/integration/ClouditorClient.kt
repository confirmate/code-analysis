/*
 * This file is part of the Confirmate project.
 */
package de.fraunhofer.aisec.confirmate.integration

import de.fraunhofer.aisec.codyze.AnalysisResult
import io.clouditor.api.EvidenceStoreApi
import io.clouditor.api.OrchestratorApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ClouditorClient {
    val log: Logger = LoggerFactory.getLogger(ClouditorClient::class.java)

    suspend fun sendClouditorResults(result: AnalysisResult) {
        val clouditorResults = result.toClouditorResults()
        for (assessment in clouditorResults.first) {
            val response = OrchestratorApi().orchestratorStoreAssessmentResult(assessment)
            if (response.success) {
                log.info(
                    "Successfully sent assessment result with id ${assessment.id} to Clouditor."
                )
            } else {
                log.error(
                    "Failed to send assessment result with id ${assessment.id} to Clouditor: ${response.response}"
                )
            }
            println("Assessment: $assessment")
        }

        for (evidence in clouditorResults.second) {
            val response = EvidenceStoreApi().evidenceStoreStoreEvidence(evidence)
            if (response.success) {
                log.info("Successfully sent evidence with id ${evidence.id} to Clouditor.")
            } else {
                log.error(
                    "Failed to send evidence with id ${evidence.id} to Clouditor: ${response.response}"
                )
            }
        }
    }
}
