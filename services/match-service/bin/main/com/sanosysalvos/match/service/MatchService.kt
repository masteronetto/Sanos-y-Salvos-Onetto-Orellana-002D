package com.sanosysalvos.match.service

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.concurrent.CopyOnWriteArrayList
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

data class PetReportEvent(
    val reportId: String,
    val petName: String,
    val species: String,
    val breed: String,
    val color: String,
    val size: String,
    val lat: Double,
    val lng: Double,
    val status: String,
    val timestamp: Long,
)

data class CoincidenciaDetectadaEvent(
    val reportId1: String,
    val reportId2: String,
    val score: Int,
    val timestamp: Long,
)

@Service
class MatchService(
    private val rabbitTemplate: RabbitTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${rabbitmq.exchange}") private val exchange: String,
    @Value("\${rabbitmq.routing-key.match}") private val matchRoutingKey: String,
) {
    private val reports = CopyOnWriteArrayList<PetReportEvent>()

    @RabbitListener(queues = ["\${rabbitmq.queue.pet-reports}"])
    fun processPetReport(message: String) {
        try {
            val report = objectMapper.readValue(message, PetReportEvent::class.java)
            reports.add(report)
            evaluateMatches(report)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun evaluateMatches(incomingReport: PetReportEvent) {
        val oppositeStatus = if (incomingReport.status == "LOST") "FOUND" else "LOST"

        reports.forEach { existingReport ->
            if (existingReport.status == oppositeStatus) {
                val score = calculateMatchScore(incomingReport, existingReport)

                if (score >= 70) {
                    val matchEvent = CoincidenciaDetectadaEvent(
                        reportId1 = incomingReport.reportId,
                        reportId2 = existingReport.reportId,
                        score = score,
                        timestamp = System.currentTimeMillis(),
                    )
                    rabbitTemplate.convertAndSend(
                        exchange,
                        matchRoutingKey,
                        objectMapper.writeValueAsString(matchEvent),
                    )
                }
            }
        }
    }

    private fun calculateMatchScore(report1: PetReportEvent, report2: PetReportEvent): Int {
        var score = 0
        if (report1.breed.lowercase() == report2.breed.lowercase()) {
            score += 40
        }
        if (report1.color.lowercase() == report2.color.lowercase()) {
            score += 30
        }
        if (report1.size.uppercase() == report2.size.uppercase()) {
            score += 20
        }
        val distanceKm = haversineDistance(report1.lat, report1.lng, report2.lat, report2.lng)
        if (distanceKm <= 5.0) {
            score += 10
        }
        return score
    }

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) * 
                Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2)

        val c = 2 * Math.asin(Math.sqrt(a))
        return earthRadius * c
    }
}

