package com.example.myprojectcitypulse.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myprojectcitypulse.model.Lieux
import com.example.myprojectcitypulse.notifications.NotificationHelper
import com.example.myprojectcitypulse.service.LocationUtils
import com.google.android.gms.location.LocationServices

class NotifiFragment : Fragment() {

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private val fusedLocationProvider by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        demanderPermission()
        NotificationHelper.createChannel(requireContext())

        obtenirPositionEtVerifier()
    }

    private fun demanderPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val granted = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun obtenirPositionEtVerifier() {

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationProvider.lastLocation.addOnSuccessListener { location ->

            if (location != null) {

                verifierLieuxProches(
                    location.latitude,
                    location.longitude,
                    obtenirLieux()
                )
            }
        }
    }

    //
    private fun obtenirLieux(): List<Lieux> {

        return listOf(
            Lieux(
                idlieu = 1,
                nomlieu = "Test Place",
                adresse = "Port-au-Prince",
                photo = "",
                categorie = "Test",
                latitude = 18.54,
                longitude = -72.34
            )
        )
    }

    private fun verifierLieuxProches(
        userLat: Double,
        userLng: Double,
        lieux: List<Lieux>
    ) {

        for (lieu in lieux) {

            if (LocationUtils.isWithin500m(
                    userLat,
                    userLng,
                    lieu.latitude,
                    lieu.longitude
                )
            ) {

                NotificationHelper.showNotification(
                    requireContext(),
                    "Lieu proche détecté",
                    "Le lieu ${lieu.nomlieu} est à moins de 500m"
                )
            }
        }
    }
}