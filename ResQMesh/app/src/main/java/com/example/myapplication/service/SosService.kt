package com.example.myapplication.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import com.example.myapplication.screens.KEY_SOS_SESSION_ID
import com.example.myapplication.screens.PREFS_NAME
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.util.Date

class SosService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        Log.d("SOS_DEBUG", "A. SosService onCreate()")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("SOS_DEBUG", "5. SosService.onStartCommand() received action: ${intent?.action}")
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return START_NOT_STICKY
    }

    private fun start() {
        Log.d("SOS_DEBUG", "6. Service start() method initiated.")
        val channelId = "sos_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "SOS Service", NotificationManager.IMPORTANCE_DEFAULT)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SOS Mode Active")
            .setContentText("Transmitting location to emergency services.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        startForeground(1, notification)

        Log.d("SOS_DEBUG", "6A. Getting initial location before creating session...")
        try {
            LocationServices.getFusedLocationProviderClient(applicationContext)
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        Log.d("SOS_DEBUG", "6B. Initial location received. Creating Firestore document.")
                        val testUserId = android.provider.Settings.Secure.getString(applicationContext.contentResolver,android.provider.Settings.Secure.ANDROID_ID)

                        val newSessionId = "sos_${testUserId}"
                        val initialLocationMap = mapOf("latitude" to location.latitude, "longitude" to location.longitude)

                        // HACKATHON SHORTCUT: Use the same consistent test ID as the messaging service.

                        val sosSession = SosSession(
                            userId = testUserId,
                            status = "ACTIVE",
                            startTime = Date(),
                            lastHeartbeat = Date(),
                            currentLocation = initialLocationMap,
                            batteryLevel = 100 // Placeholder
                        )

                        Firebase.firestore.collection("sos_sessions").document(newSessionId)
                            .set(sosSession)
                            .addOnSuccessListener {
                                Log.d("SOS_DEBUG", "8. SUCCESS! Initial Firestore document created for user $testUserId.")
                                prefs.edit().putString(KEY_SOS_SESSION_ID, newSessionId).apply()
                                startHeartbeatLoop(newSessionId)
                            }
                            .addOnFailureListener { e ->
                                Log.e("SOS_DEBUG", "8. FAILURE! Could not write to Firestore.", e)
                                stopSelf()
                            }
                    } else {
                        Log.e("SOS_DEBUG", "Failed to get initial location, cannot start SOS.")
                        stopSelf()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("SOS_DEBUG", "Error getting initial location.", e)
                    stopSelf()
                }
        } catch (e: SecurityException) {
            Log.e("SOS_DEBUG", "Location permission missing, cannot start service.", e)
            stopSelf()
        }
    }

    private fun startHeartbeatLoop(sessionId: String) {
        Log.d("SOS_DEBUG", "9. Starting heartbeat loop for session: $sessionId")
        serviceScope.launch {
            while (isActive) {
                delay(60_000)
                try {
                    LocationServices.getFusedLocationProviderClient(applicationContext)
                        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                        .addOnSuccessListener { location ->
                            if (location != null) {
                                val updates = mapOf(
                                    "lastHeartbeat" to Date(),
                                    "currentLocation" to mapOf("latitude" to location.latitude, "longitude" to location.longitude),
                                    "batteryLevel" to 100
                                )
                                Firebase.firestore.collection("sos_sessions").document(sessionId)
                                    .update(updates)
                                    .addOnSuccessListener { Log.d("SOS_DEBUG", "Heartbeat sent successfully.") }
                                    .addOnFailureListener { e -> Log.e("SOS_DEBUG", "Heartbeat failed.", e) }
                            }
                        }
                } catch (e: SecurityException) {
                    Log.e("SOS_DEBUG", "Location permission missing for heartbeat", e)
                    stopSelf()
                } catch (e: Exception) {
                    Log.e("SOS_DEBUG", "An error occurred in the heartbeat loop", e)
                }
            }
        }
    }

    private fun stop() {
        Log.d("SOS_DEBUG", "Stop action received. Cancelling service.")
        val sessionId = prefs.getString(KEY_SOS_SESSION_ID, null)
        if (sessionId != null) {
            val finalUpdate = mapOf("status" to "RESOLVED", "lastHeartbeat" to Date())
            Firebase.firestore.collection("sos_sessions").document(sessionId)
                .update(finalUpdate)
                .addOnCompleteListener {
                    prefs.edit().remove(KEY_SOS_SESSION_ID).apply()
                    serviceScope.cancel()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
        } else {
            serviceScope.cancel()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d("SOS_DEBUG", "Z. SosService onDestroy()")
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}

data class SosSession(
    val userId: String,
    val status: String,
    val startTime: Date,
    val lastHeartbeat: Date,
    val currentLocation: Map<String, Double>? = null,
    val batteryLevel: Int
)