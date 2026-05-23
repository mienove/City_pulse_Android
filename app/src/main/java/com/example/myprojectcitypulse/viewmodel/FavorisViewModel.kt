package com.example.myprojectcitypulse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myprojectcitypulse.data.local.Favori
import com.example.myprojectcitypulse.repository.FavorisRepository
import kotlinx.coroutines.launch

class FavorisViewModel(private val repo: FavorisRepository) : ViewModel() {

    val favoris = repo.getFavoris()

    fun addFavori(favori: Favori) {
        viewModelScope.launch {
            repo.addFavori(favori)
        }
    }

    fun deleteFavori(favori: Favori) {
        viewModelScope.launch {
            repo.deleteFavori(favori)
        }
    }
}