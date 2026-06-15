package com.sanosysalvos.pet.api

import com.sanosysalvos.contracts.PetProfile
import com.sanosysalvos.contracts.PetSize
import com.sanosysalvos.contracts.PetSpecies
import com.sanosysalvos.pet.service.PetService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PetControllerTest {
    private val petService = mockk<PetService>()
    private val controller = PetController(petService)

    @Test
    fun `health returns service status`() {
        assertEquals(mapOf("service" to "pet-service", "status" to "up"), controller.health())
    }

    @Test
    fun `listPets returns pet list`() {
        val pet = PetProfile("1", "owner1", "Fido", PetSpecies.DOG, "Labrador", 3, "brown", PetSize.MEDIUM, null, null)
        every { petService.listPets() } returns listOf(pet)

        assertEquals(listOf(pet), controller.listPets())
    }

    @Test
    fun `createPet delegates creation to service`() {
        val request = PetProfile(null, "owner1", "Fido", PetSpecies.DOG, "Labrador", 3, "brown", PetSize.MEDIUM, null, null)
        val created = request.copy(id = "PET-1234")
        every { petService.createPet(request) } returns created

        assertEquals(created, controller.createPet(request))
    }
}
