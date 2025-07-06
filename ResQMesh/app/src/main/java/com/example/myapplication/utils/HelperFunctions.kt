package com.example.myapplication.utils

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import com.example.myapplication.data.UserStatus
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun getStatusIcon(status: UserStatus) = when (status) {
    UserStatus.RESOLVED -> Icons.Default.CheckCircle
    UserStatus.UNRESOLVED -> Icons.Default.Warning
    UserStatus.CRITICAL -> Icons.Default.Error
}

fun getStatusColor(status: UserStatus) = when (status) {
    UserStatus.RESOLVED -> Color.Green
    UserStatus.UNRESOLVED -> Color(0xFFFF9800) // Orange
    UserStatus.CRITICAL -> Color.Red
}

fun calculateDistance(point1: LatLng, point2: LatLng): Double {
    val earthRadius = 6371.0 // Earth's radius in kilometers

    val lat1Rad = Math.toRadians(point1.latitude)
    val lat2Rad = Math.toRadians(point2.latitude)
    val deltaLatRad = Math.toRadians(point2.latitude - point1.latitude)
    val deltaLngRad = Math.toRadians(point2.longitude - point1.longitude)

    val a = sin(deltaLatRad / 2).pow(2) +
            cos(lat1Rad) * cos(lat2Rad) *
            sin(deltaLngRad / 2).pow(2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadius * c
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatTime(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val minutes = ChronoUnit.MINUTES.between(dateTime, now)

    return when {
        minutes < 60 -> "$minutes min ago"
        minutes < 1440 -> "${minutes / 60} hr ago"
        else -> "${minutes / 1440} days ago"
    }
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)