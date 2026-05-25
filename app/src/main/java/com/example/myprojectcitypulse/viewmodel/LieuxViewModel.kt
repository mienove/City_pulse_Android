package com.example.myprojectcitypulse.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myprojectcitypulse.model.Lieux
import com.example.myprojectcitypulse.repository.LieuxRepository
import kotlinx.coroutines.launch

class LieuxViewModel(private val repository: LieuxRepository) : ViewModel() {

    private var listeComplete: List<Lieux> = emptyList()

    private val _lieux = MutableLiveData<List<Lieux>>()
    val lieux: LiveData<List<Lieux>> = _lieux

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun chargerLieux() {
        viewModelScope.launch {
            _loading.postValue(true) // Safe background dispatch
            try {
                val data = repository.getLieux()
                listeComplete = data
                _lieux.postValue(data)
            } catch (e: Exception) {
                _error.postValue("Erreur de chargement: ${e.message}")
            } finally {
                _loading.postValue(false)
            }
        }
    }

    fun filtreParCategorie(categorie: String) {
        if (categorie == "Tous") {
            _lieux.value = listeComplete
        } else {
            _lieux.value = listeComplete.filter {
                it.categorie.contains(categorie, ignoreCase = true)
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

    fun chargerLieuxProches(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _loading.postValue(true)
            try {
                val lieuxProches = repository.getLieuxProches(latitude, longitude)
                listeComplete = lieuxProches
                _lieux.postValue(lieuxProches)
            } catch (e: Exception) {
                _error.postValue("Erreur lors du chargement des lieux à proximité: ${e.message}")
            } finally {
                _loading.postValue(false)
            }
        }
    }

    fun toggleFavori(lieuId: Long, estFavori: Boolean) {
        viewModelScope.launch {
            if (estFavori) {
                repository.ajouterFavori(lieuId)
            } else {
                repository.supprimerFavori(lieuId)
            }

            // OPTIMIZATION: Update memory list to prevent heavy database re-reading
            listeComplete = listeComplete.map {
                if (it.idlieu == lieuId) it.copy(estFavori = if (estFavori) 1 else 0) else it
            }
            _lieux.postValue(listeComplete)
        }
    }

    fun sauvegarderNote(lieuId: Long, note: String) {
        viewModelScope.launch {
            repository.sauvegarderNote(lieuId, note)

            // OPTIMIZATION: Inline update memory list state
            listeComplete = listeComplete.map {
                if (it.idlieu == lieuId) it.copy(notePersonnelle = note) else it
            }
            _lieux.postValue(listeComplete)
        }
    }
}
