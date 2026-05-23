package com.example.myprojectcitypulse.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FavorisDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favori: Favori)

    @Delete
    suspend fun delete(favori: Favori)

    @Query("SELECT * FROM favoris")
    fun getAllFavoris(): LiveData<List<Favori>>
}