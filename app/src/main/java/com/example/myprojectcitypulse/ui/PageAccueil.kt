package com.example.myprojectcitypulse.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.myprojectcitypulse.R



class PageAccueil : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bouton = view.findViewById<Button>(R.id.btnOuvrirMap)

        bouton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PageMap())
                .addToBackStack(null)
                .commit()
        }
    }
}