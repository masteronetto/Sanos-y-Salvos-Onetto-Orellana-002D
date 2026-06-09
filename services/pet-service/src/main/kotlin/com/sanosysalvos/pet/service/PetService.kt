package com.sanosysalvos.pet.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.sanosysalvos.contracts.PetProfile
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

data class MascotaReportadaEvent(
    val reportId: String,
    val petName: String,
    val species: String,
    val breed: String,
    val color: String,
    val size: String,
    val lat: Double,
    val lng: Double,
    val status: String,
    val timestamp: Long = System.currentTimeMillis(),
)

@Service
class PetService(
    private val rabbitTemplate: RabbitTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${rabbitmq.exchange}") private val exchange: String,
    @Value("\${rabbitmq.routing-key.pet}") private val routingKey: String,
) {
    private val petProfiles = CopyOnWriteArrayList<PetProfile>()

    fun listPets(): List<PetProfile> = petProfiles.toList()

    fun createPet(request: PetProfile): PetProfile {
        val created = request.copy(id = request.id ?: "PET-${UUID.randomUUID().toString().take(8)}")
        petProfiles.add(created)

        // Publish Mascota_Reportada event to RabbitMQ
        val event = MascotaReportadaEvent(
            reportId = created.id ?: "UNKNOWN",
            petName = created.name,
            species = created.species.toString(),
            breed = created.breed ?: "Desconocido",
            color = created.color,
            size = created.size.toString(),
            lat = 0.0, // Would come from location data in real implementation
            lng = 0.0,
            status = "ACTIVE",
            timestamp = System.currentTimeMillis(),
        )
        rabbitTemplate.convertAndSend(exchange, routingKey, objectMapper.writeValueAsString(event))

        return created
    }
}
