//2.5
package com.example.myprojectcitypulse.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myprojectcitypulse.R
import com.example.myprojectcitypulse.model.Lieux
import com.example.myprojectcitypulse.viewmodel.LieuxViewModel

class DetailFragment : Fragment() {

    // ViewModel partagé avec l'activité
    private lateinit var viewModel: LieuxViewModel

    // Lieu sélectionné
    private lateinit var lieu: Lieux

    // UI
    private lateinit var txtNom: TextView
    private lateinit var txtAdresse: TextView
    private lateinit var txtCategorie: TextView
    private lateinit var txtCoordonnees: TextView
    private lateinit var btnShare: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_detail_partage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialisation ViewModel (IMPORTANT)
        viewModel = ViewModelProvider(requireActivity())[LieuxViewModel::class.java]

        // Liaison UI (doit correspondre EXACTEMENT au XML)
        txtNom = view.findViewById(R.id.txtNomLieu)
        txtAdresse = view.findViewById(R.id.txtAdresse)
        txtCategorie = view.findViewById(R.id.txtCategorie)
        txtCoordonnees = view.findViewById(R.id.txtCoordonnees)
        btnShare = view.findViewById(R.id.btnShare)

        // Observer le lieu sélectionné
        viewModel.selectedLieu.observe(viewLifecycleOwner) { data ->
            data?.let {
                lieu = it
                afficherLieu()
            }
        }

        // Bouton partager
        btnShare.setOnClickListener {
            partagerLieu()
        }
    }

    // Affichage des infos
    private fun afficherLieu() {
        txtNom.text = lieu.nomlieu
        txtAdresse.text = lieu.adresse
        txtCategorie.text = lieu.categorie
        txtCoordonnees.text = "${lieu.latitude}, ${lieu.longitude}"
    }

    // Fonction de partage (WhatsApp / SMS / Email)
    private fun partagerLieu() {

        val message = """
            Nom : ${lieu.nomlieu}
            Adresse : ${lieu.adresse}
            Catégorie : ${lieu.categorie}

            Coordonnées :
            ${lieu.latitude}, ${lieu.longitude}

            Google Maps :
            https://maps.google.com/?q=${lieu.latitude},${lieu.longitude}
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }

        startActivity(Intent.createChooser(intent, "Partager via"))
    }
}