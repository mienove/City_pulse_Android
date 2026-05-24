// service/LocationForegroundService.kt
package com.example.myprojectcitypulse.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.example.myprojectcitypulse.R



class LocationForegroundService : Service() {

    private lateinit var locationCallback: LocationCallback

    companion object {
        const val CHANNEL_ID = "location_channel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    private fun createNotificationChannel() {
        // Supprimer la condition SDK_INT car API 26+ (Android 8+)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Service de localisation",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CityPulse")
            .setContentText("Recherche de lieux à proximité...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(10000L)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    sendLocationToApp(location)
                    //2.6
                    checkNearbyPlaces(location)
                }
            }
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun sendLocationToApp(location: android.location.Location) {
        val intent = Intent("LOCATION_UPDATE")
        intent.putExtra("latitude", location.latitude)
        intent.putExtra("longitude", location.longitude)
        sendBroadcast(intent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    //2.6
    private fun checkNearbyPlaces(location: android.location.Location) {

        // TEMPORAIRE (on test d'abord avec 1 lieu fictif)
        val testLieu = com.example.myprojectcitypulse.model.Lieux(
            idlieu = 1,
            nomlieu = "Test Lieu",
            adresse = "Quelque part",
            photo = "",
            categorie = "Test",
            latitude = location.latitude + 0.001,
            longitude = location.longitude + 0.001,
            estFavori = 0,
            notePersonnelle = ""
        )

        val results = FloatArray(1)

        android.location.Location.distanceBetween(
            location.latitude,
            location.longitude,
            testLieu.latitude,
            testLieu.longitude,
            results
        )

        val distance = results[0]

        if (distance <= 500) {

            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager

            val notification =
                androidx.core.app.NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Lieu proche")
                    .setContentText("${testLieu.nomlieu} à ${distance.toInt()} m")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .build()

            notificationManager.notify(1, notification)
        }
    }
}