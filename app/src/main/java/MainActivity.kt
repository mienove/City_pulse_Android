package com.example.myprojectcitypulse

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.myprojectcitypulse.R
import com.example.myprojectcitypulse.ui.FavorisFragment
import com.example.myprojectcitypulse.ui.PageAccueil
import com.example.myprojectcitypulse.ui.PageLieux
import com.example.myprojectcitypulse.ui.theme.ThemeManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        ThemeManager.apply(ThemeManager.load(this))

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PageAccueil())
                .commit()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNav.setOnItemSelectedListener {

            when(it.itemId) {

                R.id.nav_map -> {

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, PageAccueil())
                        .commit()

                    true
                }

                R.id.nav_list -> {

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, PageLieux())
                        .commit()

                    true
                }

                R.id.nav_favoris -> {

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, FavorisFragment())
                        .commit()

                    true
                }

                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.light -> {
                ThemeManager.save(this, ThemeManager.LIGHT)
                ThemeManager.apply(ThemeManager.LIGHT)
                true
            }

            R.id.dark -> {
                ThemeManager.save(this, ThemeManager.DARK)
                ThemeManager.apply(ThemeManager.DARK)
                true
            }

            R.id.system -> {
                ThemeManager.save(this, ThemeManager.SYSTEM)
                ThemeManager.apply(ThemeManager.SYSTEM)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}