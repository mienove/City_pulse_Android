// -- LieuxViewModel.kt  --

package com.example.myprojectcitypulse.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myprojectcitypulse.model.Lieux
import com.example.myprojectcitypulse.repository.LieuxRepository
import kotlinx.coroutines.launch

class LieuxViewModel(private val repository: LieuxRepository) : ViewModel() {

    // Garde une copie des lieux
    private var listeComplete: List<Lieux> = emptyList()

    private val _lieux = MutableLiveData<List<Lieux>>()
    val lieux: LiveData<List<Lieux>> = _lieux

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // ========== MÉTHODES EXISTANTES ==========

    fun chargerLieux() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val data = repository.getLieux()
                listeComplete = data
                _lieux.value = data
            } catch (e: Exception) {
                _error.value = "Erreur de chargement: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun filtreParCategorie(categorie: String) {
        if (categorie == "Tous") {
            _lieux.value = listeComplete
        } else {
            _lieux.value = listeComplete.filter {
                it.categorie.equals(categorie, ignoreCase = true)
            }
        }
    }

    fun rechercher(text: String) {
        if (text.isEmpty()) {
            _lieux.value = listeComplete
        } else {
            _lieux.value = listeComplete.filter {
                it.nomlieu.contains(text, ignoreCase = true)
            }
        }
    }

    // ========== MÉTHODES POUR LA CARTE ==========

    /**
     * Charge les lieux à proximité d'une position GPS
     */
    fun chargerLieuxProches(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val lieuxProches = repository.getLieuxProches(latitude, longitude)
                _lieux.value = lieuxProches
                listeComplete = lieuxProches
            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement des lieux à proximité: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // ========== MÉTHODES POUR FAVORIS ET NOTES (à appeler depuis PageDetail) ==========

    fun toggleFavori(lieuId: Long, estFavori: Boolean) {
        viewModelScope.launch {
            if (estFavori) {
                repository.ajouterFavori(lieuId)
            } else {
                repository.supprimerFavori(lieuId)
            }
            // Rafraîchir la liste
            chargerLieux()
        }
    }

    fun sauvegarderNote(lieuId: Long, note: String) {
        viewModelScope.launch {
            repository.sauvegarderNote(lieuId, note)
        }
    }
}