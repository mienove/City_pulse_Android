package com.example.myprojectcitypulse.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.myprojectcitypulse.R

class PageMap : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) getUserLocation()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.idmap) as SupportMapFragment

        mapFragment.getMapAsync(this)
//button pour
        val btnSwitch = view.findViewById<Button>(R.id.btnSwitchToList)

        btnSwitch.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PageLieux())
                .addToBackStack(null) // revenir à la carte avec le bouton retour
                .commit()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getUserLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {

        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val userLatLng = LatLng(it.latitude, it.longitude)

                map.addMarker(
                    MarkerOptions().position(userLatLng).title("Vous êtes ici")
                )

                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(userLatLng, 15f)
                )
            }
        }
    }
}