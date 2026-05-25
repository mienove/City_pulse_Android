package com.example.myprojectcitypulse.repository

import com.example.myprojectcitypulse.data.local.FavorisDao
import com.example.myprojectcitypulse.data.local.Favori

class FavorisRepository(private val dao: FavorisDao) {

    fun getFavoris() = dao.getAllFavoris()

    suspend fun addFavori(favori: Favori) {
        dao.insert(favori)
    }

    suspend fun deleteFavori(favori: Favori) {
        dao.delete(favori)
    }
}