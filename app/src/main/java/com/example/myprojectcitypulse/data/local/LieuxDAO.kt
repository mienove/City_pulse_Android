package com.example.myprojectcitypulse.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myprojectcitypulse.model.Lieux

@Dao
interface LieuxDAO {

    // IGNORE conflicts on initial insert to protect existing user updates
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(lieux: List<Lieux>): List<Long>

    @Update
    suspend fun updateAll(lieux: List<Lieux>)

    // Smart transaction: inserts new items, updates matching API values, preserves user notes/favorites
    @Transaction
    suspend fun insertAll(lieux: List<Lieux>) {
        val insertResults = insertAllIgnore(lieux)
        val updateList = mutableListOf<Lieux>()

        for (i in insertResults.indices) {
            if (insertResults[i] == -1L) { // Item already exists in SQLite cache
                val currentLieu = lieux[i]
                val existingLieu = getLieuById(currentLieu.idlieu)

                if (existingLieu != null) {
                    // Retain user states while updating details from Overpass
                    updateList.add(currentLieu.copy(
                        estFavori = existingLieu.estFavori,
                        notePersonnelle = existingLieu.notePersonnelle
                    ))
                }
            }
        }
        if (updateList.isNotEmpty()) {
            updateAll(updateList)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLieu(lieu: Lieux)

    @Delete
    suspend fun deleteLieu(lieu: Lieux)

    @Update
    suspend fun updateLieu(lieu: Lieux)

    @Query("SELECT * FROM lieux WHERE idlieu = :lieuId")
    suspend fun getLieuById(lieuId: Long): Lieux?

    @Query("UPDATE lieux SET estFavori = :estFavori WHERE idlieu = :lieuId")
    suspend fun updateFavoriStatus(lieuId: Long, estFavori: Int)

    @Query("UPDATE lieux SET notePersonnelle = :note WHERE idlieu = :lieuId")
    suspend fun updateNotePersonnelle(lieuId: Long, note: String)

    @Query("SELECT * FROM lieux")
    fun getAllLieux(): LiveData<List<Lieux>>

    @Query("SELECT * FROM lieux")
    suspend fun getAllLieuxSync(): List<Lieux>
}
