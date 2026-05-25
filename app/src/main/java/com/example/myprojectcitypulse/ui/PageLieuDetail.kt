package com.example.myprojectcitypulse.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myprojectcitypulse.R
import com.example.myprojectcitypulse.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PageLieuDetail : Fragment(R.layout.fragment_fichelieudetail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        val nom = arguments?.getString("nom")
        val categorie = arguments?.getString("categorie")
        val adresse = arguments?.getString("adresse")

        val latitude = arguments?.getDouble("latitude")
        val longitude = arguments?.getDouble("longitude")

        val idLieu = arguments?.getLong("idlieu") ?: 0L

        val noteEditText =
            view.findViewById<EditText>(R.id.noteEditText)

        val noteAffichee =
            view.findViewById<TextView>(R.id.noteAffichee)

        view.findViewById<TextView>(R.id.nomLieu).text = nom

        view.findViewById<TextView>(R.id.categorieLieu).text =
            categorie

        view.findViewById<TextView>(R.id.adresseLieu).text =
            adresse

        view.findViewById<TextView>(R.id.coordonneesLieu).text =
            "Lat: $latitude , Lon: $longitude"

        val db = AppDatabase.getDatabase(requireContext())

        // Charger note existante
        lifecycleScope.launch(Dispatchers.IO) {

            val lieu =
                db.lieuxDAO().getLieuById(idLieu)

            withContext(Dispatchers.Main) {

                val note =
                    lieu?.notePersonnelle ?: ""

                noteEditText.setText(note)

                noteAffichee.text = note
            }
        }

        // Sauvegarder note
        view.findViewById<Button>(R.id.btnSaveNote)
            .setOnClickListener {

                val note =
                    noteEditText.text.toString()

                lifecycleScope.launch(Dispatchers.IO) {

                    db.lieuxDAO()
                        .updateNotePersonnelle(
                            idLieu,
                            note
                        )

                    withContext(Dispatchers.Main) {

                        noteAffichee.text = note

                        Toast.makeText(
                            requireContext(),
                            "Note sauvegardée",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }
}