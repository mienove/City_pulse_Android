package com.example.myprojectcitypulse.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myprojectcitypulse.data.local.FavorisDAO
import com.example.myprojectcitypulse.model.Favoris
import kotlinx.coroutines.launch

class FavorisViewModel(
    private val dao: FavorisDAO
) : ViewModel() {

    // Liste des favoris depuis Room
    val favoris = dao.getAllFavoris()

    // =========================
    // AJOUTER UN FAVORI
    // =========================
    fun insert(favoris: Favoris) {

        viewModelScope.launch {

            dao.insertFavoris(favoris)
        }
    }

    // =========================
    // SUPPRIMER UN FAVORI
    // =========================
    fun delete(favoris: Favoris) {

        viewModelScope.launch {

            dao.deleteFavoris(favoris)
        }
    }

    // =========================
    // MODIFIER UN FAVORI
    // =========================
    fun update(favoris: Favoris) {

        viewModelScope.launch {

            dao.updateFavoris(favoris)
        }
    }

    // =========================
    // RECHERCHER UN FAVORI PAR ID
    // =========================
    suspend fun getFavorisById(id: Int): Favoris? {

        return dao.getFavorisById(id)
    }
}