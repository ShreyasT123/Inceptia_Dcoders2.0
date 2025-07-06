package com.example.myapplication.data

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

// This enum is now in its own file and can be imported anywhere
enum class UserStatus {
    UNRESOLVED,
    RESOLVED,
    CRITICAL
}

// The DetectedUser data class also goes here
data class DetectedUser(
    val id: String,
    val name: String,
    val position: LatLng,
    val status: UserStatus,
    val lastSeen: LocalDateTime,
    val batteryLevel: Int,
)