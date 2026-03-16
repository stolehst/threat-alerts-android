package com.example.alertapp

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.alertapp.fcm.DeviceRegistrationWorker
import com.example.alertapp.fcm.DeviceTokenHolder
import com.example.alertapp.fcm.AlertAppFirebaseMessagingService
import com.example.alertapp.ui.AlertAppNav
import com.example.alertapp.ui.theme.AlertAppTheme
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or not, we still try to register */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        requestFcmTokenAndRegister()

        val openAlertId = intent?.getStringExtra(AlertAppFirebaseMessagingService.EXTRA_ALERT_ID)

        setContent {
            AlertAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AlertAppNav(startAlertId = openAlertId)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Якщо відкрили з пушу — можна оновити навігацію (опційно через ViewModel/state)
    }

    private fun requestFcmTokenAndRegister() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) return@addOnCompleteListener
                task.result?.let { token ->
                    DeviceTokenHolder.saveToken(this, token)
                    DeviceRegistrationWorker.enqueueRegister(this, token)
                }
            }
    }
}
