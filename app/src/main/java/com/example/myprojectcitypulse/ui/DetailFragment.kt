package com.example.myprojectcitypulse.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myprojectcitypulse.R

class DetailFragment : Fragment(R.layout.fragment_detail_partage) {

    private var idlieu: Long = -1L
    private var nom: String = ""
    private var adresse: String = ""
    private var categorie: String = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtNom = view.findViewById<TextView>(R.id.txtNomLieu)
        val txtAdresse = view.findViewById<TextView>(R.id.txtAdresse)
        val txtCategorie = view.findViewById<TextView>(R.id.txtCategorie)
        val txtCoordonnees = view.findViewById<TextView>(R.id.txtCoordonnees)
        val btnShare = view.findViewById<Button>(R.id.btnShare)

        // Récupération des données du Bundle
        arguments?.let {
            idlieu = it.getLong("idlieu")
            nom = it.getString("nom") ?: "Non renseigné"
            adresse = it.getString("adresse") ?: "Non renseignée"
            categorie = it.getString("categorie") ?: "Non définie"
            latitude = it.getDouble("latitude")
            longitude = it.getDouble("longitude")
        }

        // Affichage
        txtNom.text = nom
        txtAdresse.text = adresse
        txtCategorie.text = categorie

        txtCoordonnees.text = getString(
            R.string.coordonnees_format,
            latitude.toString(),
            longitude.toString()
        )

        // Partage Google Maps
        btnShare.setOnClickListener {

            val lienMaps =
                "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"

            val message = """
                $nom

                Adresse :
                $adresse

                Catégorie :
                $categorie

                Coordonnées :
                $latitude, $longitude

                Google Maps :
                $lienMaps
            """.trimIndent()

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }

            startActivity(Intent.createChooser(intent, "Partager via"))
        }
    }
}