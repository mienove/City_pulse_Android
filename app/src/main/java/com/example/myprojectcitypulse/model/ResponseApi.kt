import com.example.myprojectcitypulse.model.Lieux

data class ResponseApi(
    val elements: List<Element>
) {
    fun toLieuxList(): List<Lieux> {
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

                notePersonnelle =  element.tags?.get("description") ?: ""
            )
        }
    }
}


data class Element(
    val id: Long,
    val lat: Double,
    val lon: Double,
    val tags: Map<String, String>?
)
