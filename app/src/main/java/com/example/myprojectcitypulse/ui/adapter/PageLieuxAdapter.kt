package com.example.myprojectcitypulse.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myprojectcitypulse.R
import com.example.myprojectcitypulse.model.Lieux

class PageLieuxAdapter(
    private var lieuData: List<Lieux>,
    private val onItemClick: (Lieux) -> Unit,
    private val onFavoriClick: (Lieux) -> Unit   // ⭐ AJOUT
) : RecyclerView.Adapter<PageLieuxAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textName: TextView = view.findViewById(R.id.textName)
        val categorieText: TextView = view.findViewById(R.id.categorieText)
        val btnFavori: ImageButton = view.findViewById(R.id.btnFavori) // ⭐ AJOUT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.element_lieu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lieu = lieuData[position]

        holder.textName.text = lieu.nomlieu
        holder.categorieText.text = lieu.categorie

        // clic sur item
        holder.itemView.setOnClickListener {
            onItemClick(lieu)
        }

        // ⭐ clic sur favori
        holder.btnFavori.setOnClickListener {
            onFavoriClick(lieu)
        }
    }

    override fun getItemCount() = lieuData.size

    fun updateDonnees(newData: List<Lieux>) {
        lieuData = newData
        notifyDataSetChanged()
    }
}