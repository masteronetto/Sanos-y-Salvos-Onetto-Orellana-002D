package com.sanosysalvos.geo.service

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.concurrent.CopyOnWriteArrayList
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

data class UbicacionActualizadaEvent(
    val reportId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
)

data class LocationData(
    val reportId: String,
    val latitude: Double,
    val longitude: Double,
)

@Service
class GeoService(
    private val rabbitTemplate: RabbitTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${rabbitmq.exchange}") private val exchange: String,
    @Value("\${rabbitmq.routing-key.location}") private val locationRoutingKey: String,
) {
    private val locations = CopyOnWriteArrayList<LocationData>()

    fun updateLocation(reportId: String, latitude: Double, longitude: Double) {
        // Check if location exists, update or add
        val existing = locations.find { it.reportId == reportId }
        if (existing != null) {
            locations.remove(existing)
        }
        locations.add(LocationData(reportId, latitude, longitude))

        // Publish Ubicacion_Actualizada event
        val event = UbicacionActualizadaEvent(
            reportId = reportId,
            latitude = latitude,
            longitude = longitude,
            timestamp = System.currentTimeMillis(),
        )
        rabbitTemplate.convertAndSend(exchange, locationRoutingKey, objectMapper.writeValueAsString(event))
    }

    fun getLocation(reportId: String): LocationData? = locations.find { it.reportId == reportId }
}
