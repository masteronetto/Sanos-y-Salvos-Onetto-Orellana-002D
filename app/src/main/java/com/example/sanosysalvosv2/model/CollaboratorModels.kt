package com.example.sanosysalvosv2.model

data class CollaboratorRequest(
    val name: String,
    val type: String,
    val email: String,
    val phone: String,
    val comuna: String,
    val address: String,
    val status: String,
)

data class CollaboratorResponse(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val email: String = "",
    val phone: String = "",
    val comuna: String = "",
    val address: String = "",
    val status: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
)

data class CollaboratorsListWrapper(
    val itemsReceived: Int = 0,
    val curPage: Int = 0,
    val nextPage: Int? = null,
    val prevPage: Int? = null,
    val offset: Int = 0,
    val perPage: Int = 0,
    val items: List<CollaboratorResponse> = emptyList()
)
