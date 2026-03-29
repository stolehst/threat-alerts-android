package com.example.alertapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.alertapp.api.ActivateRequest
import com.example.alertapp.api.ApiProvider
import com.example.alertapp.auth.AuthTokenStore
import com.example.alertapp.fcm.DeviceRegistrationWorker
import com.example.alertapp.fcm.DeviceTokenHolder
import com.example.alertapp.ui.theme.CardSurface
import com.example.alertapp.ui.theme.DarkBackground
import com.example.alertapp.ui.theme.OnDarkBackground
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivationScreen(onActivated: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var code by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun activate() {
        scope.launch {
            loading = true
            error = null
            val result = withContext(Dispatchers.IO) {
                try {
                    val api = ApiProvider.getAlertApi(context)
                    val resp = api.activate(ActivateRequest(code.trim()))
                    if (resp.isSuccessful && resp.body()?.api_token?.isNotBlank() == true) {
                        resp.body()!!.api_token to null
                    } else {
                        null to "Nieprawidłowy kod"
                    }
                } catch (e: Exception) {
                    null to (e.message ?: "Błąd sieci")
                }
            }
            val token = result.first
            val err = result.second
            if (!token.isNullOrBlank()) {
                AuthTokenStore.setApiToken(context, token)
                val fcm = DeviceTokenHolder.getToken(context)
                if (!fcm.isNullOrBlank()) {
                    DeviceRegistrationWorker.enqueueRegister(context, fcm)
                }
                onActivated()
            } else {
                error = err
            }
            loading = false
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Aktywacja", color = OnDarkBackground) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardSurface,
                    titleContentColor = OnDarkBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DarkBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Wprowadź kod aktywacyjny",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnDarkBackground
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Kod") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error != null) {
                        Text(error!!, color = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = { if (!loading) activate() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = code.isNotBlank() && !loading
                    ) {
                        if (loading) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.padding(2.dp))
                        } else {
                            Text("Aktywuj")
                        }
                    }
                }
            }
        }
    }
}

