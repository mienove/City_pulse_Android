package com.example.myprojectcitypulse.data.remote

import com.example.myprojectcitypulse.model.Lieux
import com.example.myprojectcitypulse.model.OverpassResponse

/**
 * Convertit une réponse Overpass en liste d'objets Lieux
 */
fun OverpassResponse.toLieuxList(): List<Lieux> {
    return elements.mapNotNull { element ->
        try {
            // Récupérer le nom (priorité à name, sinon null)
            val nom = element.tags?.get("name")
                ?: element.tags?.get("brand")
                ?: element.tags?.get("operator")
                ?: "Sans nom"

            // Déterminer la catégorie
            val categorie = when {
                element.tags?.containsKey("amenity") == true -> element.tags["amenity"]!!
                element.tags?.containsKey("shop") == true -> element.tags["shop"]!!
                element.tags?.containsKey("tourism") == true -> element.tags["tourism"]!!
                element.tags?.containsKey("leisure") == true -> element.tags["leisure"]!!
                else -> "autre"
            }

            // Récupérer l'adresse (optionnel)
            val adresse = buildString {
                element.tags?.get("addr:street")?.let { append(it) }
                element.tags?.get("addr:housenumber")?.let { append(" $it") }
                if (isNotEmpty() && element.tags?.get("addr:city") != null) append(", ")
                element.tags?.get("addr:city")?.let { append(it) }
            }

            Lieux(
                idlieu = element.id,
                nomlieu = nom,
                adresse = adresse.ifEmpty { "Adresse non disponible" },
                photo = "",  // À remplacer plus tard si disponible
                categorie = categorie,
                latitude = element.lat,
                longitude = element.lon,
                estFavori = 0,
                notePersonnelle = ""
            )
        } catch (e: Exception) {
            null // Ignorer les éléments invalides
        }
    }
}