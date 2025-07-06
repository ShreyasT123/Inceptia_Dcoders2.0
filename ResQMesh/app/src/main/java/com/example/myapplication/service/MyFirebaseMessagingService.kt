package com.example.myapplication.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "Refreshed token: $token")
        sendTokenToServer(token)
    }

// ... inside the class

    private fun sendTokenToServer(token: String) {
        // This MUST match the ID used in your SosService
        val testUserId = android.provider.Settings.Secure.getString(applicationContext.contentResolver,android.provider.Settings.Secure.ANDROID_ID)


        Log.d("FCM_TOKEN", "Attempting to save token for hardcoded user: $testUserId")

        val tokenInfo = hashMapOf(
            "token" to token,
            "userId" to testUserId,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        // The collection name must be exactly "fcmTokens"
        Firebase.firestore.collection("fcmTokens").document(token)
            .set(tokenInfo)
            .addOnSuccessListener { Log.d("FCM_TOKEN", "SUCCESS: Token saved for $testUserId") }
            .addOnFailureListener { e -> Log.e("FCM_TOKEN", "FAILURE: Error saving token", e) }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM_MESSAGE", "From: ${remoteMessage.from}")

        remoteMessage.notification?.let {
            Log.d("FCM_MESSAGE", "Message Notification Body: ${it.body}")
            showNotification(it.title, it.body)
        }
    }

    private fun showNotification(title: String?, body: String?) {
        val channelId = "disaster_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(channelId, "Disaster Alerts", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(0, notificationBuilder.build())
    }
}