package com.example.myprojectcitypulse.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myprojectcitypulse.R
import com.example.myprojectcitypulse.data.local.AppDatabase
import com.example.myprojectcitypulse.data.local.Favori
import com.example.myprojectcitypulse.repository.FavorisRepository
import com.example.myprojectcitypulse.viewmodel.FavorisViewModel
import com.example.myprojectcitypulse.viewmodel.FavorisViewModelFactory
import com.example.myprojectcitypulse.ui.adapter.FavorisAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        viewModel.favoris.observe(viewLifecycleOwner) { list ->

            if (list.isNullOrEmpty()) {
                emptyText.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyText.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.submitList(list)
            }
        }

        // ✅ SWIPE ICI (IMPORTANT)
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val favori = adapter.getItem(viewHolder.adapterPosition)

                CoroutineScope(Dispatchers.IO).launch {
                    dao.delete(favori)
                }
            }
        }

        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)
    }
}