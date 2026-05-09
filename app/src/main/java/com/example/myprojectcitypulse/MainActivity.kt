package com.example.myprojectcitypulse


import com.example.myprojectcitypulse.ui.PageAccueil
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //  on affiche la page d'accueil
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PageAccueil())
                .commit()
        }
    }
}
