package com.sanosysalvos.pet.api

import com.sanosysalvos.contracts.PetProfile
import com.sanosysalvos.contracts.PetSize
import com.sanosysalvos.contracts.PetSpecies
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/pets")
class PetController {
    private val petProfiles = CopyOnWriteArrayList<PetProfile>()

    @GetMapping("/health")
    fun health(): Map<String, String> = mapOf(
        "service" to "pet-service",
        "status" to "up",
    )

    @GetMapping
    fun listPets(): List<PetProfile> =
        if (petProfiles.isEmpty()) defaultPets() else petProfiles.toList()

    @PostMapping
    fun createPet(@RequestBody request: PetProfile): PetProfile {
        val created = request.copy(id = request.id ?: "PET-${UUID.randomUUID().toString().take(8)}")
        petProfiles.add(created)
        return created
    }

    private fun defaultPets(): List<PetProfile> = listOf(
        PetProfile(
            id = "PET-1001",
            ownerId = "U001",
            name = "Luna",
            species = PetSpecies.DOG,
            breed = "Mestizo",
            ageYears = 3,
            color = "Marron",
            size = PetSize.MEDIUM,
        ),
        PetProfile(
            id = "PET-1002",
            ownerId = "U002",
            name = "Michi",
            species = PetSpecies.CAT,
            breed = "Criollo",
            ageYears = 2,
            color = "Blanco",
            size = PetSize.SMALL,
        ),
    )
}
