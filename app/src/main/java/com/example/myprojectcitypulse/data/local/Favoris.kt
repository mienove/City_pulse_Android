package com.example.myprojectcitypulse.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favoris")
data class Favori(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nom: String,
    val categorie: String,
    val adresse: String
)