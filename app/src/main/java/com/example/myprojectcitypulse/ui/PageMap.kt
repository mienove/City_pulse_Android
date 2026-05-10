package com.example.myprojectcitypulse.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myprojectcitypulse.R
import com.example.myprojectcitypulse.data.remote.RetrofitClient
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class PageMapTest : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var btnSwitchToList: Button
    private lateinit var btnCenterLocation: Button
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    companion object {
        private const val TAG = "CITY_PULSE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialiser le launcher de permission
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                enableMyLocation()
            } else {
                Toast.makeText(requireContext(), "Permission de localisation refusée", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView appelé")
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated appelé")

        // Initialisation des boutons
        btnSwitchToList = view.findViewById(R.id.btnSwitchToList)
        btnCenterLocation = view.findViewById(R.id.btnCenterLocation)

        btnSwitchToList.setOnClickListener {
            Toast.makeText(requireContext(), "Page Liste (bientôt disponible)", Toast.LENGTH_SHORT).show()
        }

        btnCenterLocation.setOnClickListener {
            centerOnUserLocation()
        }

        // Initialisation de la carte
        try {
            val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
            mapFragment.getMapAsync(this)
            Log.d(TAG, "getMapAsync appelé")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur: ${e.message}", e)
            Toast.makeText(requireContext(), "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        Log.d(TAG, "onMapReady appelé !!!")
        Toast.makeText(requireContext(), "Carte chargée !", Toast.LENGTH_SHORT).show()

        // Ajouter un marqueur de test
        val testLocation = LatLng(18.5392, -72.3364)
        mMap.addMarker(MarkerOptions().position(testLocation).title("Test CityPulse - Port-au-Prince"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(testLocation, 12f))

        // Charger les lieux à proximité
        loadNearbyPlaces(18.5392, -72.3364)

        // Demander la permission de localisation
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                enableMyLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(
                    requireContext(),
                    "Activez la localisation pour voir les lieux autour de vous",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            Toast.makeText(requireContext(), "Localisation activée", Toast.LENGTH_SHORT).show()
        }
    }

    private fun centerOnUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(requireContext(), "Fonctionnalité à venir", Toast.LENGTH_SHORT).show()
        } else {
            checkLocationPermission()
        }
    }

    private fun loadNearbyPlaces(lat: Double, lng: Double) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Chargement des lieux à proximité...")

                val query = """
                    [out:json][timeout:25];
                    (
                        nwr(around:1000,$lat,$lng)["amenity"];
                        nwr(around:1000,$lat,$lng)["shop"];
                        nwr(around:1000,$lat,$lng)["tourism"];
                    );
                    out body;
                """.trimIndent()

                val response = RetrofitClient.apiService.getLieux(query)

                if (response.isSuccessful && response.body() != null) {
                    val elements = response.body()!!.elements
                    Log.d(TAG, "Nombre de lieux trouvés: ${elements.size}")

                    elements.forEach { element ->
                        val position = LatLng(element.lat, element.lon)
                        val nom = element.tags?.get("name") ?: "Sans nom"
                        val categorie = element.tags?.get("amenity")
                            ?: element.tags?.get("shop")
                            ?: element.tags?.get("tourism")
                            ?: "Lieu"

                        mMap.addMarker(
                            MarkerOptions()
                                .position(position)
                                .title(nom)
                                .snippet(categorie)
                        )
                    }

                    Toast.makeText(requireContext(), "${elements.size} lieux chargés", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "Réponse API non valide")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erreur chargement lieux: ${e.message}")
                Toast.makeText(requireContext(), "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}