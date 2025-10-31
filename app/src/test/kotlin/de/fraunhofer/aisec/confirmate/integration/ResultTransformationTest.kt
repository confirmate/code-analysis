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
import de.fraunhofer.aisec.cpg.query.QueryTree
import io.clouditor.model.*
import io.ktor.util.logging.Logger
import io.mockk.*
import java.time.OffsetDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalUuidApi::class)
class ResultTransformationTest {
    private lateinit var mockLogger: Logger
    private lateinit var mockTranslationResult: TranslationResult
    private val now = OffsetDateTime.now()

    @BeforeEach
    fun setUp() {
        mockLogger = mockk(relaxed = true)
        mockTranslationResult = mockk(relaxed = true)
    }

    @Test
    fun `toAssessmentResult returns AssessmentResult when metricId and value are present`() {
        val queryTree =
            mockk<QueryTree<Boolean>>(relaxed = true) {
                every { metricId } returns "metric-1"
                every { value } returns true
                every { id } returns Uuid.random()
                every { node } returns null
                every { stringRepresentation } returns "Test String Representation"
                every { children } returns emptyList()
            }
        val requirementId = "req-1"
        val evidenceId = "evid-1"
        val toe = mockTranslationResult
        val currentTimestamp = now
        val result =
            with(currentTimestamp) {
                with(toe) { queryTree.toAssessmentResult(requirementId, evidenceId) }
            }
        assertEquals(1, result.size)
        val assessment = result.first()
        assertEquals("metric-1", assessment.metricId)
        assertEquals(true, assessment.compliant)
        assertEquals(evidenceId, assessment.evidenceId)
        assertEquals("Code", assessment.resourceTypes.first())
        assertTrue(assessment.complianceComment.contains("Test String Representation"))
    }

    @Test
    fun `toAssessmentResult recurses into children when metricId is null`() {
        val child =
            mockk<QueryTree<Boolean>>(relaxed = true) {
                every { metricId } returns "metric-2"
                every { value } returns false
                every { id } returns Uuid.random()
                every { node } returns null
                every { stringRepresentation } returns "Child String Representation"
                every { children } returns emptyList()
            }
        val parent =
            mockk<QueryTree<Boolean>>(relaxed = true) {
                every { metricId } returns null
                // every { value } returns null
                every { children } returns listOf(child)
            }
        val requirementId = "req-2"
        val evidenceId = "evid-2"
        val toe = mockTranslationResult
        val currentTimestamp = now
        val result =
            with(currentTimestamp) {
                with(toe) { parent.toAssessmentResult(requirementId, evidenceId) }
            }
        assertEquals(1, result.size)
        assertEquals("metric-2", result.first().metricId)
        assertEquals(false, result.first().compliant)
    }

    @Test
    fun `toEvidence creates Evidence with correct fields`() {
        val resource =
            Resource(application = Application(id = "app-1", name = "App1", raw = "code"))
        val toe = mockTranslationResult
        val evidence = with(toe) { resource.toEvidence() }
        assertNotNull(evidence.id)
        assertEquals(toe.id.toString(), evidence.targetOfEvaluationId)
        assertEquals(resource, evidence.resource)
        assertEquals("Codyze", evidence.toolId)
        assertNotNull(evidence.timestamp)
    }

    @Test
    fun `toConfirmateResult aggregates evidences and assessment results`() {
        val component =
            mockk<Component>(relaxed = true) {
                every { name.localName } returns "AppComponent"
                every { id } returns Uuid.random()
                every { code } returns "code"
            }
        val translationResult =
            mockk<TranslationResult>(relaxed = true) {
                every { components } returns mutableListOf(component)
                every { id } returns Uuid.random()
            }
        val queryTree =
            mockk<QueryTree<Boolean>>(relaxed = true) {
                every { metricId } returns "metric-3"
                every { value } returns true
                every { id } returns Uuid.random()
                every { node } returns null
                every { stringRepresentation } returns "Assessment String"
                every { children } returns emptyList()
            }
        val requirementsResults = mapOf("req-3" to queryTree)
        val analysisResult =
            mockk<AnalysisResult>(relaxed = true) {
                every { this@mockk.translationResult } returns translationResult
                every { this@mockk.requirementsResults } returns requirementsResults
            }
        val result = with(mockLogger) { analysisResult.toConfirmateResult() }
        assertTrue(result.assessmentResult.any { it.metricId == "metric-3" })
        assertTrue(result.evidences.isNotEmpty())
    }
}
