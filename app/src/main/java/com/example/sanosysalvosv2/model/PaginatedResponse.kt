package com.example.sanosysalvosv2.model

data class PaginatedResponse<T>(
    val items: List<T>,
    val curPage: Int,
    val nextPage: Int?,
    val prevPage: Int?,
    val itemsReceived: Int,
)
