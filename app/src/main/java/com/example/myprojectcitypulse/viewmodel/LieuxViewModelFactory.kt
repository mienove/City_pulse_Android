// -- LieuxViewModelFactory.kt --

package com.example.myprojectcitypulse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myprojectcitypulse.repository.LieuxRepository

class LieuxViewModelFactory(
    private val repository: LieuxRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LieuxViewModel::class.java)) {
            return LieuxViewModel(repository) as T
        }
        throw IllegalArgumentException("Classe ViewModel inconnue")
    }
}
