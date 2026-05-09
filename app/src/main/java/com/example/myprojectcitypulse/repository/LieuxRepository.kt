package com.example.myprojectcitypulse.repository

import android.util.Log
import com.example.myprojectcitypulse.data.local.LieuxDAO
import com.example.myprojectcitypulse.data.remote.ApiService
import com.example.myprojectcitypulse.model.Lieux

class LieuxRepository (
    private val api: ApiService,
    private val dao: LieuxDAO
){

    suspend fun getLieux(): List<Lieux>{
        return try{
            // Requête Overpass pour trouver les "amenity" (services) autour de Port-au-Prince
            val query = """
                [out:json][timeout:25];
                nwr(around:1000,18.5392, -72.3364)["amenity"];
                out;
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
}

