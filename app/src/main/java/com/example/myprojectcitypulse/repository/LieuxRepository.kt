package com.example.myprojectcitypulse.repository

import android.util.Log
import com.example.myprojectcitypulse.data.local.LieuxDAO
import com.example.myprojectcitypulse.data.remote.ApiService
import com.example.myprojectcitypulse.model.Lieux
import com.example.myprojectcitypulse.model.OverpassResponse
import com.example.myprojectcitypulse.model.toLieuxList
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class LieuxRepository(
    private val api: ApiService,
    private val dao: LieuxDAO
) {
    private val gson = Gson()

    // récupère les lieux (API + cache)
    suspend fun getLieux(): List<Lieux> = withContext(Dispatchers.IO) {
        try {
            val query = "[out:json][timeout:25];(nwr(around:3000,18.5392,-72.3364)[\"amenity\"];nwr(around:3000,18.5392,-72.3364)[\"tourism\"];nwr(around:3000,18.5392,-72.3364)[\"shop\"];nwr(around:3000,18.5392,-72.3364)[\"leisure\"];);out center;"

            val requestBody = query.toRequestBody("text/plain".toMediaType())

            val response = api.getLieux(requestBody)
            Log.d("API_TEST", "CODE = ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val jsonString = response.body()!!.string()
                val overpassResponse = gson.fromJson(jsonString, OverpassResponse::class.java)

                val lieux = overpassResponse.toLieuxList()
                Log.d("API_TEST", "NB LIEUX = ${lieux.size}")
                dao.insertAll(lieux)
                lieux
            } else {
                dao.getAllLieuxSync()
            }
        } catch (e: Exception) {
            Log.e("CITY_PULSE", "Erreur réseau ou parsing", e)
            dao.getAllLieuxSync()
        }
    }

    /**
     * Récupère les lieux à proximité d'une position GPS
     */
    suspend fun getLieuxProches(
        latitude: Double,
        longitude: Double,
        rayonMeters: Int = 100
    ): List<Lieux> = withContext(Dispatchers.IO) {
        try {
            val query = "[out:json][timeout:25];(nwr(around:$rayonMeters,$latitude,$longitude)[\"amenity\"];nwr(around:$rayonMeters,$latitude,$longitude)[\"tourism\"];nwr(around:$rayonMeters,$latitude,$longitude)[\"leisure\"];nwr(around:$rayonMeters,$latitude,$longitude)[\"shop\"];);out center;"

            val requestBody = query.toRequestBody("text/plain".toMediaType())
            val response = api.getLieux(requestBody)

            if (response.isSuccessful && response.body() != null) {
                val jsonString = response.body()!!.string()
                val overpassResponse = gson.fromJson(jsonString, OverpassResponse::class.java)

                val lieux = overpassResponse.toLieuxList()
                Log.d("API_TEST", "Nombre de lieux : ${lieux.size}")
                dao.insertAll(lieux)
                lieux
            } else {
                Log.e("API_TEST", "Erreur API : ${response.errorBody()?.string()}")
                recupererDepuisCacheParProximite(latitude, longitude, rayonMeters)
            }
        } catch (e: Exception) {
            Log.e("CITY_PULSE", "Erreur getLieuxProches", e)
            recupererDepuisCacheParProximite(latitude, longitude, rayonMeters)
        }
    }

    /**
     * Récupère tous les lieux pour la carte)
     */
    suspend fun getAllLieuxSync(): List<Lieux> = withContext(Dispatchers.IO) {
        try {
            dao.getAllLieuxSync()
        } catch (e: Exception) {
            Log.e("CITY_PULSE", "Erreur getAllLieuxSync", e)
            emptyList()
        }
    }

    /**
     * Récupère les lieux depuis l'API avec une position spécifique
     */
    suspend fun fetchLieuxByLocation(latitude: Double, longitude: Double): List<Lieux> = withContext(Dispatchers.IO) {
        try {
            val query = "[out:json][timeout:25];(nwr(around:1000,$latitude,$longitude)[\"amenity\"];nwr(around:1000,$latitude,$longitude)[\"tourism\"];nwr(around:1000,$latitude,$longitude)[\"leisure\"];nwr(around:1000,$latitude,$longitude)[\"shop\"];);out center;"

            val requestBody = query.toRequestBody("text/plain".toMediaType())
            val response = api.getLieux(requestBody)

            if (response.isSuccessful && response.body() != null) {
                val jsonString = response.body()!!.string()
                val overpassResponse = gson.fromJson(jsonString, OverpassResponse::class.java)

                val lieux = overpassResponse.toLieuxList()
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

    private suspend fun recupererDepuisCacheParProximite(lat: Double, lon: Double, rayon: Int): List<Lieux> {
        return dao.getAllLieuxSync().filter { lieu ->
            val results = FloatArray(1)
            android.location.Location.distanceBetween(lat, lon, lieu.latitude, lieu.longitude, results)
            results[0] <= rayon
        }
    }

    // ========== MÉTHODES POUR LES FAVORIS ==========

    suspend fun ajouterFavori(lieuId: Long) = withContext(Dispatchers.IO) {
        dao.updateFavoriStatus(lieuId, 1)
    }

    suspend fun supprimerFavori(lieuId: Long) = withContext(Dispatchers.IO) {
        dao.updateFavoriStatus(lieuId, 0)
    }

    suspend fun getFavoris(): List<Lieux> = withContext(Dispatchers.IO) {
        dao.getAllLieuxSync().filter { it.estFavori == 1 }
    }

    // ========== MÉTHODES POUR LES NOTES ==========

    suspend fun sauvegarderNote(lieuId: Long, note: String) = withContext(Dispatchers.IO) {
        dao.updateNotePersonnelle(lieuId, note)
    }

    suspend fun getNote(lieuId: Long): String? = withContext(Dispatchers.IO) {
        dao.getLieuById(lieuId)?.notePersonnelle
    }
}
