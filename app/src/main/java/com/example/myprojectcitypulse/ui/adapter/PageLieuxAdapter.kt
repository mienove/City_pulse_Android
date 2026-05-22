package com.example.myprojectcitypulse.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter

import com.example.myprojectcitypulse.R
import com.example.myprojectcitypulse.model.Lieux

class PageLieuxAdapter(
    private var lieuData: List<Lieux>,
    private val onItemClick: (Lieux) -> Unit):
    RecyclerView.Adapter<PageLieuxAdapter.ViewHolder>(){


    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val textName: TextView = view.findViewById(R.id.textName)
        val categorieText: TextView= view.findViewById(R.id.categorieText)
    }
    //creer une nouvelle vue avec le gestionnaire layout
    override fun onCreateViewHolder(
        p0: ViewGroup,
        p1: Int
    ): PageLieuxAdapter.ViewHolder {
        val view = LayoutInflater.from(p0.context)
            .inflate(R.layout.element_lieu, p0, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageLieuxAdapter.ViewHolder, position: Int) {
        val lieu = lieuData[position]
        holder.textName.text = lieu.nomlieu
        holder.categorieText.text = lieu.categorie
//chaque element de la liste est cliquable
        holder.itemView.setOnClickListener {
            onItemClick(lieu)
        }
    }
    //retourne la taille des donnees Lieux
    override fun getItemCount() = lieuData.size

    fun updateDonnees(newData: List<Lieux>) {
        lieuData=newData
        notifyDataSetChanged()
    }


}