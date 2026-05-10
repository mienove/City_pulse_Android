//--PageAcceuil--

package com.example.myprojectcitypulse.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myprojectcitypulse.R

class PageAccueil : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bouton = view.findViewById<Button>(R.id.btnOuvrirMap)

        bouton.setOnClickListener {
            try {
                Log.d("CITY_PULSE", "Tentative d'ouverture de PageMap")

                // Correction: Supprimer le ".replace(...)" mal placé
                val pageMap = PageMapTest()

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, pageMap)
                    .addToBackStack(null)
                    .commit()

                Log.d("CITY_PULSE", "Transaction commise avec succès")
            } catch (e: Exception) {
                Log.e("CITY_PULSE", "Erreur lors du lancement de PageMap", e)
                Toast.makeText(requireContext(), "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}