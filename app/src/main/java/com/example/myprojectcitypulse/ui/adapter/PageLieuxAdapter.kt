package com.example.myprojectcitypulse.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter

import com.example.myprojectcitypulse.R
import com.example.myprojectcitypulse.model.Lieux

class PageLieuxAdapter(private var lieuData: List<Lieux>):
    Adapter<PageLieuxAdapter.ViewHolder>(){


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

    override fun onBindViewHolder(p0: PageLieuxAdapter.ViewHolder, p1: Int) {
        val lieu = lieuData[p1]
        p0.textName.text = lieu.nomlieu
        p0.categorieText.text = lieu.categorie
    }
    //retourne la taille des donnees Lieux
    override fun getItemCount() = lieuData.size

    fun updateDonnees(newData: List<Lieux>) {
        lieuData=newData
        notifyDataSetChanged()
    }


}