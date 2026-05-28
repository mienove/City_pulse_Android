package com.example.myprojectcitypulse.service

import android.location.Location

object LocationUtils {

    fun isWithin500m(
        userLat: Double,
        userLng: Double,
        placeLat: Double,
        placeLng: Double
    ): Boolean {

        val result = FloatArray(1)

        Location.distanceBetween(
            userLat,
            userLng,
            placeLat,
            placeLng,
            result
        )

        return result[0] <= 500
    }
}