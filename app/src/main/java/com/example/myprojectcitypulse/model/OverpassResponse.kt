package com.example.myprojectcitypulse.model

import com.google.gson.annotations.SerializedName

data class OverpassResponse(
    val elements: List<OverpassElement>
)

data class OverpassElement(
    val id: Long,
    val lat: Double,
    val lon: Double,
    val tags: Map<String, String>? = null
)