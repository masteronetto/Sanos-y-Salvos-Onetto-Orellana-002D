package com.sanosysalvos.pet.api

import com.sanosysalvos.contracts.PetProfile
import com.sanosysalvos.contracts.PetSize
import com.sanosysalvos.contracts.PetSpecies
import com.sanosysalvos.pet.service.PetService
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/pets")
class PetController(private val petService: PetService) {

    @GetMapping("/health")
    fun health(): Map<String, String> = mapOf(
        "service" to "pet-service",
        "status" to "up",
    )

    @GetMapping
    fun listPets(): List<PetProfile> = petService.listPets()

    @PostMapping
    fun createPet(@RequestBody request: PetProfile): PetProfile = petService.createPet(request)
}
