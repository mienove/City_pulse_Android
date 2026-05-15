package com.example.myprojectcitypulse.ui

import com.example.myprojectcitypulse.viewmodel.LieuxViewModel
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myprojectcitypulse.R
import com.example.myprojectcitypulse.data.local.AppDatabase
import com.example.myprojectcitypulse.data.remote.RetrofitClient
import com.example.myprojectcitypulse.repository.LieuxRepository
import com.example.myprojectcitypulse.ui.adapter.PageLieuxAdapter
import com.example.myprojectcitypulse.viewmodel.LieuxViewModelFactory

class PageLieux : Fragment(R.layout.fragment_lieu) {
    private lateinit var adapter: PageLieuxAdapter
    private lateinit var viewModel: LieuxViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. GESTION DE L'APP BAR ET FLÈCHE DE RETOUR
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_lieux)
        toolbar?.setNavigationOnClickListener {
            parentFragmentManager.popBackStack() // Revient en arrière sur la page Map
        }

        // Initialisation du RecyclerView
        adapter = PageLieuxAdapter(emptyList())
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Architecture
        val apiService = RetrofitClient.apiService
        val database = AppDatabase.getDatabase(requireContext())
        val repository = LieuxRepository(apiService, database.lieuxDAO())

        // UTILISER LA FACTORY AVEC requireActivity()
        //partager les données des favoris/notes instantanément entre la liste et la carte
        val factory = LieuxViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), factory)[LieuxViewModel::class.java]

        // OBSERVER LES DONNÉES
        viewModel.lieux.observe(viewLifecycleOwner) { data ->
            adapter.updateDonnees(data)
        }

        // GESTION DU CHARGEMENT ET DES ERREURS
        val progressBar = view.findViewById<ProgressBar>(R.id.loader)
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        // FILTRAGE (Catégories)
        val spinner = view.findViewById<Spinner>(R.id.categorieSpinner)
        val categories = listOf("Tous", "restaurant", "bank", "pharmacy", "park", "cafe", "hospital")

        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selection = categories[position]
                viewModel.filtreParCategorie(selection)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // RECHERCHE (Textuelle hybride : Filtrage local + Requête API globale)
        val searchView = view.findViewById<androidx.appcompat.widget.SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String?): Boolean {
                // Filtre la liste instantanément pendant la saisie à partir de la mémoire
                viewModel.rechercher(newText ?: "")
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    // Lance une recherche Overpass globale en arrière-plan (Coordonnées par défaut de P-au-P)
                    viewModel.rechercher(query)
                    searchView.clearFocus() // Ferme le clavier virtuel après validation
                }
                return true
            }
        })

        // CHARGEMENT INITIAL (Uniquement si la liste est vide pour éviter d'écraser une recherche active)
        if (viewModel.lieux.value.isNullOrEmpty()) {
            viewModel.chargerLieux()
        }
    }
}
