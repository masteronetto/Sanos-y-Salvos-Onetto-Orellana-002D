package com.example.sanosysalvosv2.model

object ReportTypeMapper {
    private val typeToDisplay = mapOf(
        ReportTypes.LOST to "Perdida",
        ReportTypes.FOUND to "Encontrada",
    )

    private val normalizer = mapOf(
        ReportTypes.LOST to ReportTypes.LOST,
        ReportTypes.FOUND to ReportTypes.FOUND,
        "PERDIDA" to ReportTypes.LOST,
        "ENCONTRADA" to ReportTypes.FOUND,
    )

    fun normalizeType(type: String?): String? {
        if (type.isNullOrBlank()) return null
        return normalizer[type.trim().uppercase()]
    }

    fun dbToDisplay(type: String?): String {
        return normalizeType(type)?.let { typeToDisplay[it] } ?: "Desconocida"
    }

    fun displayToDb(display: String?): String? {
        return normalizeType(display)
    }

    fun isValidType(type: String?): Boolean = normalizeType(type) != null
}
