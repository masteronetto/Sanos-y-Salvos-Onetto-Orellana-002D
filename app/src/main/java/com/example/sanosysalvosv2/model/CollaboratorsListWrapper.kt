package com.example.sanosysalvosv2.model

import com.google.gson.annotations.SerializedName

data class CollaboratorsListWrapper(
    @SerializedName(value = "items", alternate = ["data", "results", "list"])
    val items: List<CollaboratorResponse>? = null
)
