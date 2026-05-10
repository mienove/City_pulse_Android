// -- LieuxRepository.kt --

package com.example.myprojectcitypulse.repository

import android.util.Log
import com.example.myprojectcitypulse.data.local.LieuxDAO
import com.example.myprojectcitypulse.data.remote.ApiService
import com.example.myprojectcitypulse.data.remote.toLieuxList
import com.example.myprojectcitypulse.model.Lieux

class LieuxRepository (
    private val api: ApiService,
    private val dao: LieuxDAO
){

    // Méthode existante - récupère les lieux (API + cache)
    suspend fun getLieux(): List<Lieux>{
        return try{
            // Requête Overpass pour trouver les "amenity" (services) autour de Port-au-Prince
            val query = """
                [out:json][timeout:25];
                nwr(around:1000,18.5392, -72.3364)["amenity"];
                out body;
            """.trimIndent()

            val response = api.getLieux(query)

            if(response.isSuccessful && response.body() != null){
                // Conversion du JSON
                val lieux = response.body()!!.toLieuxList()
                dao.insertAll(lieux) // Mise en cache Room
                lieux
            } else {
                dao.getAllLieuxSync() // Erreur API : retour au cache
            }
        } catch(e: Exception){
            Log.e("CITY_PULSE", "Erreur réseau ou parsing", e)
            dao.getAllLieuxSync() // Pas d'internet : retour au cache
        }
    }

    // ========== AJOUTS POUR LA CARTE ==========

    /**
     * Récupère les lieux à proximité d'une position GPS
     * @param latitude Latitude de l'utilisateur
     * @param longitude Longitude de l'utilisateur
     * @param rayonMeters Rayon de recherche en mètres (défaut: 1000m)
     */
    suspend fun getLieuxProches(
        latitude: Double,
        longitude: Double,
        rayonMeters: Int = 1000
    ): List<Lieux> {
        return try {
            val query = """
                [out:json][timeout:25];
                (
                    nwr(around:$rayonMeters,$latitude,$longitude)["amenity"];
                    nwr(around:$rayonMeters,$latitude,$longitude)["tourism"];
                    nwr(around:$rayonMeters,$latitude,$longitude)["leisure"];
                    nwr(around:$rayonMeters,$latitude,$longitude)["shop"];
                );
                out body;
            """.trimIndent()

            val response = api.getLieux(query)

            if (response.isSuccessful && response.body() != null) {
                val lieux = response.body()!!.toLieuxList()
                // On insère sans supprimer les anciens
                dao.insertAll(lieux)
                lieux
            } else {
                // Si l'API échoue, on filtre les lieux en cache par proximité
                dao.getAllLieuxSync().filter { lieu ->
                    calculerDistance(latitude, longitude, lieu.latitude, lieu.longitude) <= rayonMeters
                }
            }
        } catch (e: Exception) {
            Log.e("CITY_PULSE", "Erreur getLieuxProches", e)
            // Filtrage des lieux en cache par proximité
            dao.getAllLieuxSync().filter { lieu ->
                calculerDistance(latitude, longitude, lieu.latitude, lieu.longitude) <= rayonMeters
            }
        }
    }

    /**
     * Récupère tous les lieux de manière synchrone (pour la carte)
     */
    suspend fun getAllLieuxSync(): List<Lieux> {
        return try {
            dao.getAllLieuxSync()
        } catch (e: Exception) {
            Log.e("CITY_PULSE", "Erreur getAllLieuxSync", e)
            emptyList()
        }
    }

    /**
     * Récupère les lieux depuis l'API avec une position spécifique
     * Utilisé pour mettre à jour la carte après déplacement
     */
    suspend fun fetchLieuxByLocation(latitude: Double, longitude: Double): List<Lieux> {
        return try {
            val query = """
                [out:json][timeout:25];
                (
                    nwr(around:1000,$latitude,$longitude)["amenity"];
                    nwr(around:1000,$latitude,$longitude)["tourism"];
                    nwr(around:1000,$latitude,$longitude)["leisure"];
                    nwr(around:1000,$latitude,$longitude)["shop"];
                );
                out body;
            """.trimIndent()

            val response = api.getLieux(query)

            if (response.isSuccessful && response.body() != null) {
                val lieux = response.body()!!.toLieuxList()
                dao.insertAll(lieux)
                lieux
            } else {
                getLieuxProches(latitude, longitude)
            }
        } catch (e: Exception) {
            Log.e("CITY_PULSE", "Erreur fetchLieuxByLocation", e)
            getLieuxProches(latitude, longitude)
        }
    }

    /**
     * Calcule la distance entre deux points GPS (formule de Haversine)
     * @return Distance en mètres
     */
    private fun calculerDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    // ========== MÉTHODES POUR LES FAVORIS ==========

    /**
     * Ajoute un lieu aux favoris
     */
    suspend fun ajouterFavori(lieuId: Long) {
        dao.updateFavoriStatus(lieuId, 1)
    }

    /**
     * Supprime un lieu des favoris
     */
    suspend fun supprimerFavori(lieuId: Long) {
        dao.updateFavoriStatus(lieuId, 0)
    }

    /**
     * Récupère tous les favoris
     */
    suspend fun getFavoris(): List<Lieux> {
        return dao.getAllLieuxSync().filter { it.estFavori == 1 }
    }

    // ========== MÉTHODES POUR LES NOTES ==========

    /**
     * Sauvegarde une note personnelle pour un lieu
     */
    suspend fun sauvegarderNote(lieuId: Long, note: String) {
        dao.updateNotePersonnelle(lieuId, note)
    }

    /**
     * Récupère la note d'un lieu
     */
    suspend fun getNote(lieuId: Long): String? {
        return dao.getLieuById(lieuId)?.notePersonnelle
    }
}