package com.sanosysalvos.match.api

import com.sanosysalvos.contracts.MatchEvaluationRequest
import com.sanosysalvos.contracts.MatchEvaluationResponse
import com.sanosysalvos.contracts.MatchCandidate
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MatchControllerTest {
    private val controller = MatchController()

    @Test
    fun `health returns match-service up status`() {
        assertEquals(mapOf("service" to "match-service", "status" to "up"), controller.health())
    }

    @Test
    fun `evaluate returns match evaluation response`() {
        val request = MatchEvaluationRequest("L1", "F1")

        val response = controller.evaluate(request)

        assertEquals(true, response.shouldNotify)
        assertEquals("L1", response.candidate.reportId)
        assertEquals("F1", response.candidate.matchedReportId)
    }
}
