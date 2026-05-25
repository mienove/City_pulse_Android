package com.example.myprojectcitypulse.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myprojectcitypulse.R
import com.example.myprojectcitypulse.data.local.Favori

class FavorisAdapter : RecyclerView.Adapter<FavorisAdapter.ViewHolder>() {

    private var list = listOf<Favori>()

    fun submitList(data: List<Favori>) {
        list = data
        notifyDataSetChanged()
    }
    fun getItem(position: Int): Favori {
        return list[position]
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nom: TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nom.text = list[position].nom
    }

    override fun getItemCount() = list.size
}