package com.example.myprojectcitypulse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myprojectcitypulse.repository.FavorisRepository

class FavorisViewModelFactory(
    private val repo: FavorisRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FavorisViewModel(repo) as T
    }
}