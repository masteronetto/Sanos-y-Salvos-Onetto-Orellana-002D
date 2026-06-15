package com.example.sanosysalvosv2.model

data class PetListResponse(
    val itemsReceived: Int,
    val curPage: Int,
    val nextPage: Int?,
    val prevPage: Int?,
    val offset: Int,
    val perPage: Int,
    val items: List<PetResponse>,
)
