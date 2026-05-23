package com.example.myprojectcitypulse.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myprojectcitypulse.R
import com.example.myprojectcitypulse.data.local.AppDatabase
import com.example.myprojectcitypulse.data.local.Favori
import com.example.myprojectcitypulse.repository.FavorisRepository
import com.example.myprojectcitypulse.viewmodel.FavorisViewModel
import com.example.myprojectcitypulse.viewmodel.FavorisViewModelFactory
import com.example.myprojectcitypulse.ui.adapter.FavorisAdapter

class FavorisFragment : Fragment(R.layout.fragment_favoris) {

    private lateinit var viewModel: FavorisViewModel
    private lateinit var adapter: FavorisAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val emptyText = view.findViewById<TextView>(R.id.txtEmpty)

        adapter = FavorisAdapter()

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // ROOM
        val dao = AppDatabase.getDatabase(requireContext()).favorisDao()
        val repo = FavorisRepository(dao)
        val factory = FavorisViewModelFactory(repo)

        viewModel = ViewModelProvider(this, factory)[FavorisViewModel::class.java]


        // OBSERVER LES FAVORIS
        viewModel.favoris.observe(viewLifecycleOwner) { list ->

            if (list.isNullOrEmpty()) {
                emptyText.visibility = View.VISIBLE
            } else {
                emptyText.visibility = View.GONE
            }

            adapter.submitList(list)
        }
    }
}