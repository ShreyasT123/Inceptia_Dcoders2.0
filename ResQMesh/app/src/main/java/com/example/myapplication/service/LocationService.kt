package com.example.myapplication.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task

object LocationService {
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(
        context: Context,
        onLocationResult: (Location?) -> Unit
    ) {
        val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

        val locationTask: Task<Location> = fusedLocationClient.lastLocation
        locationTask.addOnSuccessListener { location ->
            onLocationResult(location)
        }.addOnFailureListener {
            onLocationResult(null)
        }
    }
}
