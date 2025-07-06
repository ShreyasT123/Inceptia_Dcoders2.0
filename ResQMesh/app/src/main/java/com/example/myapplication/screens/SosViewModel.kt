package com.example.myapplication.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.myapplication.service.SosService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// A key to check if a session is active (e.g., using SharedPreferences)
const val PREFS_NAME = "SosPrefs"
const val KEY_SOS_SESSION_ID = "sos_session_id"

class SosViewModel : ViewModel() {

    private val _isSosActive = MutableStateFlow(false)
    val isSosActive = _isSosActive.asStateFlow()

    fun checkInitialSosState(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isSosActive.value = prefs.getString(KEY_SOS_SESSION_ID, null) != null
    }

    fun startSos(context: Context) {
        // Start the service
        Intent(context, SosService::class.java).also {
            it.action = SosService.ACTION_START
            context.startService(it)
        }
        Log.d("SosViewModel", "Sos service started")
        _isSosActive.value = true
    }

    fun stopSos(context: Context) {
        // Stop the service
        Intent(context, SosService::class.java).also {
            it.action = SosService.ACTION_STOP
            context.startService(it)
        }
        _isSosActive.value = false
    }
}