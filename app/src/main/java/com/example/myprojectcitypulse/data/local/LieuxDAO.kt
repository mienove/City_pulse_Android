//-- LieuxDAO.kt --
package com.example.myprojectcitypulse.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myprojectcitypulse.model.Lieux

@Dao
interface LieuxDAO {

    // Insérer un lieu
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLieu(lieu: Lieux)

    // Insérer plusieurs lieux
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lieux: List<Lieux>)

    // Supprimer
    @Delete
    suspend fun deleteLieu(lieu: Lieux)

    // Mise à jour
    @Update
    suspend fun updateLieu(lieu: Lieux)

    // Récupérer un lieu
    @Query("SELECT * FROM lieux WHERE idlieu = :lieuId")
    suspend fun getLieuById(lieuId: Long): Lieux?

    // Modifier favori
    @Query("UPDATE lieux SET estFavori = :estFavori WHERE idlieu = :lieuId")
    suspend fun updateFavoriStatus(
        lieuId: Long,
        estFavori: Int
    )

    // Modifier note
    @Query("UPDATE lieux SET notePersonnelle = :note WHERE idlieu = :lieuId")
    suspend fun updateNotePersonnelle(
        lieuId: Long,
        note: String
    )

    // Tous les lieux (LiveData)
    @Query("SELECT * FROM lieux")
    fun getAllLieux(): LiveData<List<Lieux>>

    // Tous les lieux sync
    @Query("SELECT * FROM lieux")
    suspend fun getAllLieuxSync(): List<Lieux>
}