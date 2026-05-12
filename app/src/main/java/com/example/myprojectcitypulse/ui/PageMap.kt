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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myprojectcitypulse.R
import com.example.myprojectcitypulse.data.remote.RetrofitClient
import com.example.myprojectcitypulse.model.OverpassElement
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class PageMap : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var btnCenterLocation: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var permissionDeniedCard: View
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private var currentLocation: Location? = null
    private var isMapReady = false
    private val placeMarkers = mutableListOf<Marker>()
    private var isLocationUpdating = false  // Pour éviter les appels multiples

    companion object {
        private const val TAG = "CITY_PULSE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        locationRequest = LocationRequest.Builder(5000L).apply {
            setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            setMinUpdateIntervalMillis(3000L)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    currentLocation = location
                    if (isMapReady && !isLocationUpdating) {
                        isLocationUpdating = true
                        updateUserLocation(location)
                        loadNearbyPlaces(location.latitude, location.longitude)
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


        btnSwitchToList.setOnClickListener{
        val listelieux = PageLieux() // Remplacez par le nom exact de votre classe de liste
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, listelieux)
            .addToBackStack(null) // revenir à la carte avec le bouton "Retour"
            .commit()
            Toast.makeText(requireContext(), "Page Liste (bientôt disponible)", Toast.LENGTH_SHORT).show()
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

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                startLocationUpdates()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(
                    requireContext(),
                    "CityPulse a besoin de votre position pour afficher les lieux autour de vous",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun showPermissionDeniedMessage() {
        permissionDeniedCard.visibility = View.VISIBLE
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

        // Obtenir la dernière position connue immédiatement
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                if (isMapReady) {
                    updateUserLocation(location)
                    loadNearbyPlaces(location.latitude, location.longitude)
                }
            } else {
                // Si pas de dernière position, demander une mise à jour forcée
                Log.d(TAG, "Dernière position inconnue, attente des mises à jour...")
                Toast.makeText(requireContext(), "Recherche de votre position...", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Log.e(TAG, "Erreur lors de la récupération de la position")
            Toast.makeText(requireContext(), "Impossible d'obtenir votre position", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUserLocation(location: Location) {
        val userLatLng = LatLng(location.latitude, location.longitude)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))

        // Ajouter un marqueur pour la position actuelle
        mMap.addMarker(
            MarkerOptions()
                .position(userLatLng)
                .title("Vous êtes ici")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
    }

    @SuppressLint("MissingPermission")
    private fun centerOnUserLocation() {
        // Vérifier si on a déjà une position
        if (currentLocation != null) {
            val userLatLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
            Toast.makeText(requireContext(), "Centrage sur votre position", Toast.LENGTH_SHORT).show()
        } else {
            // Si pas de position, essayer d'en obtenir une
            Toast.makeText(requireContext(), "Récupération de votre position...", Toast.LENGTH_SHORT).show()

            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        currentLocation = location
                        val userLatLng = LatLng(location.latitude, location.longitude)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                        Toast.makeText(requireContext(), "Centrage sur votre position", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Position non disponible. Activez le GPS.", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Impossible d'obtenir votre position", Toast.LENGTH_SHORT).show()
                }
            } else {
                checkLocationPermission()
            }
        }
    }

    private fun loadNearbyPlaces(lat: Double, lng: Double) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val query = """
                    [out:json][timeout:25];
                    (
                        nwr(around:1000,$lat,$lng)["amenity"];
                        nwr(around:1000,$lat,$lng)["shop"];
                        nwr(around:1000,$lat,$lng)["tourism"];
                        nwr(around:1000,$lat,$lng)["leisure"];
                    );
                    out body;
                """.trimIndent()

                val response = RetrofitClient.apiService.getLieux(query)

                if (response.isSuccessful && response.body() != null) {
                    val elements = response.body()!!.elements
                    Log.d(TAG, "Lieux trouvés: ${elements.size}")
                    addMarkersToMap(elements, lat, lng)
                    if (elements.isNotEmpty()) {
                        Toast.makeText(requireContext(), "${elements.size} lieux trouvés", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "Réponse API non valide")
                }
                progressBar.visibility = View.GONE
            } catch (e: Exception) {
                Log.e(TAG, "Erreur: ${e.message}")
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun addMarkersToMap(elements: List<OverpassElement>, currentLat: Double, currentLng: Double) {
        placeMarkers.forEach { it.remove() }
        placeMarkers.clear()

        Log.d(TAG, "Ajout de ${elements.size} marqueurs sur la carte")

        for (element in elements) {
            val nom = element.tags?.get("name")
                ?: element.tags?.get("brand")
                ?: "Lieu sans nom"

            val categorie = when {
                element.tags?.containsKey("amenity") == true -> element.tags["amenity"] ?: "Autre"
                element.tags?.containsKey("shop") == true -> element.tags["shop"] ?: "Autre"
                element.tags?.containsKey("tourism") == true -> element.tags["tourism"] ?: "Autre"
                element.tags?.containsKey("leisure") == true -> element.tags["leisure"] ?: "Autre"
                else -> "Lieu"
            }

            val distance = calculateDistance(currentLat, currentLng, element.lat, element.lon)
            val distanceText = if (distance < 1000) {
                "${distance.toInt()} m"
            } else {
                String.format("%.1f km", distance / 1000)
            }

            val snippet = "$categorie • $distanceText"
            val position = LatLng(element.lat, element.lon)

            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(nom)
                    .snippet(snippet)
                    .icon(getMarkerIcon(categorie))
            )

            marker?.let {
                placeMarkers.add(it)
                it.tag = element.id
            }
        }

        Log.d(TAG, "Marqueurs ajoutés: ${placeMarkers.size}")
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    private fun getMarkerIcon(categorie: String): BitmapDescriptor {
        val hue = when (categorie.lowercase()) {
            "restaurant", "cafe", "bar", "fast_food", "pub" -> BitmapDescriptorFactory.HUE_RED
            "park", "garden", "nature_reserve" -> BitmapDescriptorFactory.HUE_GREEN
            "museum", "art", "gallery" -> BitmapDescriptorFactory.HUE_ORANGE
            "shop", "mall", "supermarket", "convenience" -> BitmapDescriptorFactory.HUE_YELLOW
            "hotel", "hostel", "guest_house" -> BitmapDescriptorFactory.HUE_VIOLET
            "cinema", "theatre", "nightclub" -> BitmapDescriptorFactory.HUE_ROSE
            else -> BitmapDescriptorFactory.HUE_AZURE
        }
        return BitmapDescriptorFactory.defaultMarker(hue)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::fusedLocationClient.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}