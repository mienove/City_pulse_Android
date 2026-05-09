package com.example.myprojectcitypulse.model


import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "favoris")
data class Favoris(
    @PrimaryKey(autoGenerate = true)
    val idFavoris: Int,
    val idlieu:Int,
    val dateEnregistrement: Date,
    val idutilisateurs:Int
)
