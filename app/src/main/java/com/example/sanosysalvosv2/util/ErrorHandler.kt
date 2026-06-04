package com.example.sanosysalvosv2.util

import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

object ErrorHandler {
    fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is HttpException -> {
                when (throwable.code()) {
                    400 -> "Solicitud inválida"
                    401 -> "Sesión expirada. Por favor inicia sesión nuevamente"
                    403 -> "No tienes permisos para acceder"
                    404 -> "Recurso no encontrado"
                    500 -> "Error del servidor. Intenta más tarde"
                    502, 503, 504 -> "Servicio no disponible"
                    else -> "Error: ${throwable.code()}"
                }
            }
            is SocketTimeoutException -> "Conexión tardó demasiado. Verifica tu conexión"
            is ConnectException -> "No se puede conectar al servidor. Verifica tu conexión"
            is IOException -> "Error de conexión. Verifica tu internet"
            is IllegalStateException -> {
                when {
                    throwable.message?.contains("Sesion", ignoreCase = true) == true -> "Sesión inválida"
                    else -> throwable.message ?: "Error desconocido"
                }
            }
            else -> throwable.message ?: "Ocurrió un error inesperado"
        }
    }

    fun isNetworkError(throwable: Throwable): Boolean {
        return throwable is IOException || throwable is ConnectException || throwable is SocketTimeoutException
    }

    fun isAuthError(throwable: Throwable): Boolean {
        return (throwable is HttpException && throwable.code() in 401..403) ||
               (throwable is IllegalStateException && throwable.message?.contains("Sesion", ignoreCase = true) == true)
    }
}
