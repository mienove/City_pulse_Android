package com.example.myprojectcitypulse.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myprojectcitypulse.model.Favoris
//deuxpoints quatre
@Dao
interface FavorisDAO {

    // =========================
    // AJOUTER FAVORI
    // =========================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoris(
        favoris: Favoris
    )

    // =========================
    // SUPPRIMER FAVORI
    // =========================
    @Delete
    suspend fun deleteFavoris(
        favoris: Favoris
    )

    // =========================
    // MODIFIER FAVORI
    // =========================
    @Update
    suspend fun updateFavoris(
        favoris: Favoris
    )

    // =========================
    // RECUPERER TOUS LES FAVORIS
    // =========================
    @Query("SELECT * FROM favoris")
    fun getAllFavoris(): LiveData<List<Favoris>>

    // =========================
    // RECUPERER PAR ID
    // =========================
    @Query("SELECT * FROM favoris WHERE idFavoris = :id")
    suspend fun getFavorisById(
        id: Int
    ): Favoris?
}