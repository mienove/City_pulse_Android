package com.example.myprojectcitypulse

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.myprojectcitypulse.R
import com.example.myprojectcitypulse.ui.PageAccueil
import com.example.myprojectcitypulse.ui.theme.ThemeManager

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