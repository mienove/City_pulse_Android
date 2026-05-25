package com.example.myprojectcitypulse.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myprojectcitypulse.R
import com.example.myprojectcitypulse.data.local.AppDatabase
import com.example.myprojectcitypulse.data.remote.RetrofitClient
import com.example.myprojectcitypulse.model.Lieux
import com.example.myprojectcitypulse.repository.LieuxRepository
import com.example.myprojectcitypulse.viewmodel.LieuxViewModel
import com.example.myprojectcitypulse.viewmodel.LieuxViewModelFactory // Créez cette factory ou adaptez selon votre injection
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PageMap : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var btnCenterLocation: FloatingActionButton
    private lateinit var btnSwitchToList: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var permissionDeniedCard: View
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    // Intégration de l'architecture ViewModel
    private lateinit var viewModel: LieuxViewModel

    private var currentLocation: Location? = null
    private var lastFetchedLocation: Location? = null // Évite les requêtes Overpass redondantes
    private var isMapReady = false
    private var userLocationMarker: Marker? = null // Référence unique pour ne pas dupliquer le point bleu

    companion object {
        private const val TAG = "CITY_PULSE"
        private const val DISTANCE_SEUIL_METRES = 200f // Ne recharge l'API que si l'utilisateur bouge de 200m
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialisation de la chaîne ViewModel -> Repository -> Room / Retrofit
        val database = AppDatabase.getDatabase(requireContext())
        val apiService = RetrofitClient.apiService // Remplacez par votre instance Retrofit réelle
        val repository = LieuxRepository(apiService, database.lieuxDAO())

        // Configuration de la Factory (Si vous n'utilisez pas de framework comme Hilt)
        val factory = LieuxViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[LieuxViewModel::class.java]

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                permissionDeniedCard.visibility = View.GONE
                startLocationUpdates()
            } else {
                showPermissionDeniedMessage()
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        locationRequest = LocationRequest.Builder(10000L).apply { // Augmenté à 10s pour préserver le CPU
            setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            setMinUpdateIntervalMillis(5000L)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    currentLocation = location
                    if (isMapReady) {
                        updateUserLocationMarker(location)
                        verifierEtChargerDonnees(location)
                    }
                }
            }
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
        super.onViewCreated(view, savedInstanceState)

        btnCenterLocation = view.findViewById(R.id.btnCenterLocation)
        progressBar = view.findViewById(R.id.progressBar)
        permissionDeniedCard = view.findViewById(R.id.permissionDeniedCard)
        btnSwitchToList = view.findViewById(R.id.btnSwitchToList)

        btnSwitchToList.setOnClickListener {
            val listelieux = PageLieux()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, listelieux)
                .addToBackStack(null)
                .commit()
        }

        btnCenterLocation.setOnClickListener {
            centerOnUserLocation()
        }

        val btnOpenSettings = permissionDeniedCard.findViewById<Button>(R.id.btnOpenSettings)
        btnOpenSettings.setOnClickListener {
            openAppSettings()
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        observerViewModel()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        isMapReady = true
        configureMap()
        checkLocationPermission()
    }

    private fun configureMap() {
        mMap.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isCompassEnabled = true
            uiSettings.isMyLocationButtonEnabled = false
        }
    }

    private fun observerViewModel() {
        // Synchronisation de la barre de progression
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Interception et affichage propre des erreurs
        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (!errorMsg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
            }
        }

        // Affichage fluide des marqueurs d'Overpass / Room
        viewModel.lieux.observe(viewLifecycleOwner) { listeLieux ->
            if (!isMapReady) return@observe

            // Supprime uniquement les marqueurs de lieux (conserve le point utilisateur)
            mMap.clear()
            userLocationMarker = null
            currentLocation?.let { updateUserLocationMarker(it) }

            for (lieu in listeLieux) {
                val position = LatLng(lieu.latitude, lieu.longitude)
                mMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(lieu.nomlieu)
                        .snippet(lieu.categorie)
                )
            }
        }
    }

    private fun verifierEtChargerDonnees(location: Location) {
        val lastLocation = lastFetchedLocation
        if (lastLocation == null || location.distanceTo(lastLocation) >= DISTANCE_SEUIL_METRES) {
            lastFetchedLocation = location
            // Appel via ViewModel (géré en tâche de fond sur Dispatchers.IO)
            viewModel.chargerLieuxProches(location.latitude, location.longitude)
        }
    }

    private fun updateUserLocationMarker(location: Location) {
        val userLatLng = LatLng(location.latitude, location.longitude)

        if (userLocationMarker == null) {
            userLocationMarker = mMap.addMarker(
                MarkerOptions()
                    .position(userLatLng)
                    .title("Vous êtes ici")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            // Premier centrage de la caméra
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
        } else {
            userLocationMarker?.position = userLatLng
        }
    }

    private fun centerOnUserLocation() {
        currentLocation?.let {
            val userLatLng = LatLng(it.latitude, it.longitude)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
        } ?: Toast.makeText(requireContext(), "Position indisponible", Toast.LENGTH_SHORT).show()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun showPermissionDeniedMessage() {
        permissionDeniedCard.visibility = View.VISIBLE
        // Mode hors-ligne : On charge les données du cache global
        viewModel.chargerLieux()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${requireContext().packageName}")
        }
        startActivity(intent)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                if (isMapReady) {
                    updateUserLocationMarker(location)
                    verifierEtChargerDonnees(location)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fusedLocationClient.removeLocationUpdates(locationCallback) // Crucial pour éviter les fuites de mémoire
        userLocationMarker = null
    }
}
