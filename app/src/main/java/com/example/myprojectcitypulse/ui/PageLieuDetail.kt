package com.example.myprojectcitypulse.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myprojectcitypulse.R

class PageLieuDetail : Fragment(R.layout.fragment_fichelieudetail){
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val nom = arguments?.getString("nom");
        val categorie = arguments?.getString("categorie");
        val adresse = arguments?.getString("adresse")

        val latitude = arguments?.getDouble("latitude")
        val longitude = arguments?.getDouble("longitude")

        view.findViewById<TextView>(R.id.nomLieu).text = nom

        view.findViewById<TextView>(R.id.categorieLieu).text = categorie

        view.findViewById<TextView>(R.id.adresseLieu).text = adresse

        view.findViewById<TextView>(R.id.coordonneesLieu).text =
            "Lat: $latitude , Lon: $longitude"

    }
}