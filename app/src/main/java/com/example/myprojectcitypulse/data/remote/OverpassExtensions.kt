package com.example.myprojectcitypulse.data.remote

import com.example.myprojectcitypulse.model.Lieux
import com.example.myprojectcitypulse.model.OverpassResponse

fun OverpassResponse.toLieuxList(): List<Lieux> {

    return elements.mapNotNull { element ->

        val latitude = element.lat ?: return@mapNotNull null
        val longitude = element.lon ?: return@mapNotNull null

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