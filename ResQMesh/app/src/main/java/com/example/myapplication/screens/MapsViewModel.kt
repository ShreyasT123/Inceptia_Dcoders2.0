package com.example.myapplication.screens

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.DetectedUser
import com.example.myapplication.data.UserStatus
import com.example.myapplication.service.LocationService
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

class MapsViewModel : ViewModel() {

    private val _users = mutableStateOf<List<DetectedUser>>(emptyList())
    val users = _users

    private val _currentLocation = mutableStateOf(LatLng(0.0, 0.0))
    val currentLocation = _currentLocation

    fun fetchCurrentLocation(context: Context) {
        LocationService.getLastKnownLocation(context) { location ->
            if (location != null) {
                _currentLocation.value = LatLng(location.latitude, location.longitude)
                fetchUsers() // Now use the real currentLocation in distance calc
            } else {
                Log.w("MapsViewModel", "Location not available.")
            }
        }
    }

    private fun fetchUsers() {
        val center = _currentLocation.value

        FirebaseFirestore.getInstance().collection("sos_sessions")
            .get()
            .addOnSuccessListener { result ->
                val userList = result.mapNotNull { doc ->
                    try {
                        val userId = doc.getString("userId") ?: return@mapNotNull null
                        val battery = doc.getLong("batteryLevel")?.toInt() ?: 0

                        val locationMap = doc.get("currentLocation") as? Map<*, *> ?: return@mapNotNull null
                        val lat = locationMap["latitude"] as? Double ?: return@mapNotNull null
                        val lon = locationMap["longitude"] as? Double ?: return@mapNotNull null
                        val position = LatLng(lat, lon)

                        val lastSeen = doc.getTimestamp("lastHeartbeat")?.toDate()?.toInstant()
                            ?.atZone(ZoneId.systemDefault())?.toLocalDateTime() ?: LocalDateTime.now()

                        val status = when (doc.getString("status")?.uppercase()) {
                            "SAFE" -> UserStatus.RESOLVED
                            "CRITICAL" -> UserStatus.CRITICAL
                            "NEEDS_HELP" -> UserStatus.UNRESOLVED
                            else -> UserStatus.RESOLVED
                        }

                        DetectedUser(
                            id = userId,
                            name = userId,
                            position = position,
                            status = status,
                            lastSeen = lastSeen,
                            batteryLevel = battery
                        )
                    } catch (e: Exception) {
                        Log.e("MapsViewModel", "Parse error: ${e.message}")
                        null
                    }
                }

                Log.d("MapsViewModel", "Fetched users: ${userList.size}")
                _users.value = userList
            }
            .addOnFailureListener {
                Log.e("MapsViewModel", "Fetch error: ${it.message}")
            }
    }
}
