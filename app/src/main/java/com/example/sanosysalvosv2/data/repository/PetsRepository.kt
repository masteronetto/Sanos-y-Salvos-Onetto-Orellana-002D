package com.example.sanosysalvosv2.data.repository

import android.util.Log
import com.example.sanosysalvosv2.BuildConfig
import com.example.sanosysalvosv2.data.api.PetsApi
import com.example.sanosysalvosv2.data.api.XanoRetrofitClient
import com.example.sanosysalvosv2.model.PetRequest
import com.example.sanosysalvosv2.model.PetResponse
import java.io.IOException
import retrofit2.HttpException
import retrofit2.Response

class PetsRepository {
    private val tag = "PetsRepository"
    private fun api(): PetsApi = XanoRetrofitClient.retrofit.create(PetsApi::class.java)

    suspend fun listPets(
        token: String,
        page: Int = 1,
        perPage: Int = 20,
        species: String = "",
        breed: String = "",
    ): MapsResult<List<PetResponse>> {
        val url = "${BuildConfig.XANO_BASE_URL}api:sanos-y-salvos-pets/list?page=$page&per_page=$perPage&species=$species&breed=$breed"
        Log.d(tag, "GET $url")

        return try {
            val response = api().listPets(authHeader = "Bearer $token", page = page, perPage = perPage, species = species, breed = breed)
            if (!response.isSuccessful) {
                val errorBody = try { response.errorBody()?.string() ?: "" } catch (_: Exception) { "(error reading body)" }
                MapsResult.Error("${parseHttpError(response)} - $errorBody")
            } else {
                parsePetsListResponse(response.body())
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red al acceder a mascotas: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP al acceder a mascotas: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado al acceder a mascotas")
        }
    }

    suspend fun listMyPets(token: String): MapsResult<List<PetResponse>> {
        val url = "${BuildConfig.XANO_BASE_URL}api:sanos-y-salvos-pets/my"
        Log.d(tag, "GET $url")

        return try {
            val response = api().listMyPets(authHeader = "Bearer $token")
            if (!response.isSuccessful) {
                MapsResult.Error(parseHttpError(response))
            } else {
                parsePetsListResponse(response.body())
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red al acceder a mascotas: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP al acceder a mascotas: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado al acceder a mascotas")
        }
    }

    suspend fun listPetsByOwner(token: String, ownerId: String): MapsResult<List<PetResponse>> {
        val url = "${BuildConfig.XANO_BASE_URL}api:sanos-y-salvos-pets/list_by_owner/$ownerId"
        Log.d(tag, "GET $url")

        return try {
            val response = api().listPetsByOwner(authHeader = "Bearer $token", ownerId = ownerId)
            if (!response.isSuccessful) {
                MapsResult.Error(parseHttpError(response))
            } else {
                parsePetsListResponse(response.body())
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red al acceder a mascotas: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP al acceder a mascotas: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado al acceder a mascotas")
        }
    }

    suspend fun getPetDetails(token: String, id: String): MapsResult<PetResponse> {
        val url = "${BuildConfig.XANO_BASE_URL}api:sanos-y-salvos-pets/details/$id"
        Log.d(tag, "GET $url")
        return safeCall { api().getPetDetails(authHeader = "Bearer $token", id = id) }
    }

    suspend fun createPet(token: String, request: PetRequest): MapsResult<PetResponse> {
        val url = "${BuildConfig.XANO_BASE_URL}api:sanos-y-salvos-pets/create"
        Log.d(tag, "POST $url")
        return safeCall { api().createPet(authHeader = "Bearer $token", request = request) }
    }

    suspend fun updatePet(token: String, id: String, request: PetRequest): MapsResult<PetResponse> {
        val url = "${BuildConfig.XANO_BASE_URL}api:sanos-y-salvos-pets/update/$id"
        Log.d(tag, "PUT $url")
        return safeCall { api().updatePet(authHeader = "Bearer $token", id = id, request = request) }
    }

    suspend fun deletePet(token: String, id: String): MapsResult<Unit> {
        val url = "${BuildConfig.XANO_BASE_URL}api:sanos-y-salvos-pets/delete/$id"
        Log.d(tag, "DELETE $url")
        return try {
            val response = api().deletePet(authHeader = "Bearer $token", id = id)
            if (!response.isSuccessful) {
                MapsResult.Error(parseHttpError(response))
            } else {
                MapsResult.Success(Unit)
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red al eliminar mascota: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP al eliminar mascota: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado al eliminar mascota")
        }
    }

    private suspend fun <T> safeCall(call: suspend () -> Response<T>): MapsResult<T> {
        return try {
            val response = call()
            if (!response.isSuccessful) {
                MapsResult.Error(parseHttpError(response))
            } else {
                val body = response.body()
                if (body == null) {
                    MapsResult.Error("Respuesta de mascota vacía")
                } else {
                    MapsResult.Success(body)
                }
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red al acceder a mascotas: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP al acceder a mascotas: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado al acceder a mascotas")
        }
    }

    private fun parsePetsListResponse(raw: Any?): MapsResult<List<PetResponse>> {
        val items = when (raw) {
            is List<*> -> raw
            is Map<*, *> -> {
                val candidateKeys = listOf("items", "data", "results", "pets", "list")
                val extracted = candidateKeys.asSequence()
                    .mapNotNull { raw[it] }
                    .firstOrNull()

                when (extracted) {
                    is List<*> -> extracted
                    is Map<*, *> -> listOf(extracted)
                    else -> emptyList<Any?>()
                }
            }
            else -> emptyList<Any?>()
        }

        val pets = items.mapNotNull { item ->
            when (item) {
                is Map<*, *> -> parsePetItem(item)
                else -> null
            }
        }

        return MapsResult.Success(pets)
    }

    private fun parsePetItem(raw: Map<*, *>): PetResponse? {
        val id = raw["id"]?.toString() ?: raw["uid"]?.toString() ?: raw["pet_id"]?.toString() ?: return null
        val name = raw["name"]?.toString() ?: ""
        val species = raw["species"]?.toString() ?: ""
        val breed = raw["breed"]?.toString() ?: ""
        val sex = raw["sex"]?.toString() ?: ""
        val age = raw["age"]?.toString()?.toIntOrNull() ?: 0
        val color = raw["color"]?.toString() ?: ""
        val size = raw["size"]?.toString() ?: raw["pet_size"]?.toString() ?: "Mediano"
        val isMicrochip = when (val value = raw["hasMicrochip"] ?: raw["microchip"] ?: raw["isMicrochip"] ?: raw["is_microchip"]) {
            is Boolean -> value
            is String -> value.equals("true", ignoreCase = true)
            is Number -> value.toInt() != 0
            else -> false
        }
        val isNeutered = when (val value = raw["isNeutered"] ?: raw["is_neutered"]) {
            is Boolean -> value
            is String -> value.equals("true", ignoreCase = true)
            is Number -> value.toInt() != 0
            else -> false
        }
        val photoUrl = raw["photoUrl"]?.toString() ?: raw["photo_url"]?.toString()
        val photoBase64 = raw["photoBase64"]?.toString() ?: raw["photo_base64"]?.toString() ?: ""
        val ownerId = raw["ownerId"]?.toString() ?: raw["owner_id"]?.toString() ?: ""

        return PetResponse(
            id = id,
            name = name,
            species = species,
            breed = breed,
            sex = sex,
            age = age,
            color = color,
            isNeutered = isNeutered,
            size = size,
            hasMicrochip = isMicrochip,
            photoBase64 = photoBase64,
            photoUrl = photoUrl,
            ownerId = ownerId,
        )
    }

    private fun parseHttpError(response: Response<*>): String {
        val backendError = try {
            response.errorBody()?.string()?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
        val detail = backendError ?: response.message().ifBlank { "sin detalle" }
        return "Pets API fallido: ${response.code()} - $detail"
    }
}
