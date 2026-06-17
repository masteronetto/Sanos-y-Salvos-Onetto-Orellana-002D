package com.example.sanosysalvosv2.util

import androidx.compose.ui.graphics.Color

object TranslationUtils {
  
  fun species(value: String?): String = when(value?.uppercase()) {
    "DOG" -> "Perro"
    "CAT" -> "Gato"
    "OTHER" -> "Otro"
    else -> value?.replaceFirstChar { it.uppercase() } ?: "No especificado"
  }

  fun gender(value: String?): String = when(value?.uppercase()) {
    "MALE" -> "Macho"
    "FEMALE" -> "Hembra"
    else -> value?.replaceFirstChar { it.uppercase() } ?: "No especificado"
  }

  fun size(value: String?): String = when(value?.uppercase()) {
    "SMALL" -> "Pequeño"
    "MEDIUM" -> "Mediano"
    "LARGE" -> "Grande"
    else -> value?.replaceFirstChar { it.uppercase() } ?: "No especificado"
  }

  fun reportType(value: String?): String = when(value?.uppercase()) {
    "LOST" -> "Perdida"
    "FOUND" -> "Encontrada"
    else -> value ?: "Desconocido"
  }

  fun reportStatus(value: String?): String = when(value?.uppercase()) {
    "PENDING" -> "Pendiente"
    "APPROVED" -> "Aprobado"
    "REJECTED" -> "Rechazado"
    "RESOLVED" -> "Resuelto"
    else -> value ?: "Pendiente"
  }

  fun statusColor(value: String?): Color = when(value?.uppercase()) {
    "PENDING" -> Color(0xFFFF8C00)
    "APPROVED" -> Color(0xFF4A9B8E)
    "RESOLVED" -> Color(0xFF2E7D32)
    "REJECTED" -> Color(0xFFE53935)
    else -> Color.Gray
  }
}
