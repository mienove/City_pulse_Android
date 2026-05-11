package com.example.myprojectcitypulse.data.remote

import com.example.myprojectcitypulse.model.Lieux
import com.example.myprojectcitypulse.model.OverpassResponse

fun OverpassResponse.toLieuxList(): List<Lieux> {
    return elements.map { element ->
        Lieux(
            idlieu = element.id,
            nomlieu = element.tags?.get("name") ?: "Lieu sans nom",

            // Extraction de l'adresse
            adresse = element.tags?.get("addr:full")
                ?: "${element.tags?.get("addr:street") ?: ""} ${element.tags?.get("addr:housenumber") ?: ""}".trim()
                    .ifEmpty { "Adresse inconnue" },

            photo = "",
            categorie = element.tags?.get("amenity") ?: "Lieu",

            latitude = element.lat,
            longitude = element.lon,

            notePersonnelle = element.tags?.get("description") ?: ""
        )
    }
}
