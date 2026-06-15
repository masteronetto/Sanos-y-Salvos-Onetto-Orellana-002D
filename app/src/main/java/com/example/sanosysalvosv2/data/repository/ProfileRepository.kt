package com.example.sanosysalvosv2.data.repository

import android.util.Log
import com.example.sanosysalvosv2.BuildConfig
import com.example.sanosysalvosv2.data.api.ProfileApi
import com.example.sanosysalvosv2.data.api.XanoRetrofitClient
import com.example.sanosysalvosv2.model.UpdateProfileRequest
import com.example.sanosysalvosv2.model.UserProfile
import com.example.sanosysalvosv2.model.UserProfileResponse
import java.io.IOException
import retrofit2.HttpException
import retrofit2.Response

class ProfileRepository {
    private val tag = "ProfileRepository"

    private fun api(): ProfileApi = XanoRetrofitClient.retrofit.create(ProfileApi::class.java)

    suspend fun getMe(token: String): MapsResult<UserProfile> {
        val url = "${BuildConfig.XANO_BASE_URL}api:sanos-y-salvos-auth/me"
        Log.d(tag, "GET $url")

        return try {
            val response = api().getMe(authHeader = "Bearer $token")
            if (!response.isSuccessful) {
                MapsResult.Error(parseHttpError(response))
            } else {
                val body = response.body()
                if (body == null) {
                    MapsResult.Error("Perfil del usuario vacío")
                } else {
                    MapsResult.Success(mapUser(body))
                }
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red al cargar perfil: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP al cargar perfil: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado al cargar perfil")
        }
    }

    suspend fun updateProfile(token: String, userId: String, body: UpdateProfileRequest): MapsResult<UserProfile> {
        val url = "${BuildConfig.XANO_BASE_URL}api:sanos-y-salvos-users/update/$userId"
        Log.d(tag, "PUT $url")

        return try {
            val response = api().updateProfile(authHeader = "Bearer $token", id = userId, body = body)
            if (!response.isSuccessful) {
                MapsResult.Error(parseHttpError(response))
            } else {
                val profile = response.body()
                if (profile == null) {
                    MapsResult.Error("Perfil actualizado sin contenido")
                } else {
                    MapsResult.Success(mapUser(profile))
                }
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red al actualizar perfil: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP al actualizar perfil: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado al actualizar perfil")
        }
    }

    suspend fun logout(token: String): MapsResult<Unit> {
        val url = "${BuildConfig.XANO_BASE_URL}api:sanos-y-salvos-auth/logout"
        Log.d(tag, "POST $url")

        return try {
            val response = api().logout(authHeader = "Bearer $token")
            if (!response.isSuccessful) {
                MapsResult.Error(parseHttpError(response))
            } else {
                MapsResult.Success(Unit)
            }
        } catch (e: IOException) {
            MapsResult.Error("Error de red al cerrar sesión: ${e.message ?: "sin detalle"}")
        } catch (e: HttpException) {
            MapsResult.Error("Error HTTP al cerrar sesión: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            MapsResult.Error(e.message ?: "Error inesperado al cerrar sesión")
        }
    }

    private fun mapUser(response: UserProfileResponse): UserProfile {
        return UserProfile(
            id = response.id,
            fullName = response.fullName.orEmpty(),
            email = response.email.orEmpty(),
            phone = response.phone.orEmpty(),
            city = response.city.orEmpty(),
        )
    }

    private fun parseHttpError(response: Response<*>): String {
        val backendError = try {
            response.errorBody()?.string()?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
        val detail = backendError ?: response.message().ifBlank { "sin detalle" }
        return "Perfil API fallido: ${response.code()} - $detail"
    }
}
