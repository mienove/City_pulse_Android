package com.example.myprojectcitypulse.model

data class OverpassResponse(
    val elements: List<OverpassElement>
)

data class Center(
    val lat: Double,
    val lon: Double
)

data class OverpassElement(

    val type: String,

    val id: Long,

    val lat: Double? = null,
    val lon: Double? = null,

    val center: Center? = null,

    val tags: Map<String, String>? = null
)

fun OverpassResponse.toLieuxList(): List<Lieux> {

    return elements.mapNotNull { element ->

        // Gestion node + way + relation
        val latitude =
            element.lat ?: element.center?.lat ?: return@mapNotNull null

        val longitude =
            element.lon ?: element.center?.lon ?: return@mapNotNull null

        Lieux(

            idlieu = element.id,

            nomlieu =
                element.tags?.get("name")
                    ?: "Lieu sans nom",

            adresse =
                element.tags?.get("addr:full")
                    ?: "${element.tags?.get("addr:street") ?: ""} ${
                        element.tags?.get("addr:housenumber") ?: ""
                    }".trim().ifEmpty {
                        "Adresse inconnue"
                    },

            photo = "",

            categorie =
                element.tags?.get("amenity")
                    ?: element.tags?.get("shop")
                    ?: element.tags?.get("tourism")
                    ?: element.tags?.get("leisure")
                    ?: "Autre",

            latitude = latitude,

            longitude = longitude,

            notePersonnelle =
                element.tags?.get("description") ?: ""
        )
    }
}