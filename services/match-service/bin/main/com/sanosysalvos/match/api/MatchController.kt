package com.sanosysalvos.match.api

import com.sanosysalvos.contracts.MatchCandidate
import com.sanosysalvos.contracts.MatchEvaluationRequest
import com.sanosysalvos.contracts.MatchEvaluationResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/match")
class MatchController {

    @GetMapping("/health")
    fun health(): Map<String, String> = mapOf(
        "service" to "match-service",
        "status" to "up",
    )

    @PostMapping("/evaluate")
    fun evaluate(@RequestBody request: MatchEvaluationRequest): MatchEvaluationResponse {
        val candidate = MatchCandidate(
            reportId = request.lostReportId,
            matchedReportId = request.foundReportId,
            score = 0.82,
            reason = "Coincidencia preliminar por zona y descripcion",
        )

        return MatchEvaluationResponse(
            candidate = candidate,
            shouldNotify = candidate.score >= 0.75,
        )
    }
}
