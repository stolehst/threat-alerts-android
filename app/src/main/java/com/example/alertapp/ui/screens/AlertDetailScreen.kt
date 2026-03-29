package com.example.alertapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.alertapp.api.AlertItem
import com.example.alertapp.api.ApiProvider
import com.example.alertapp.ui.formatDetectedAtLabel
import com.example.alertapp.ui.formatThreatTypeLabel
import com.example.alertapp.ui.theme.AlertRed
import com.example.alertapp.ui.theme.CardSurface
import com.example.alertapp.ui.theme.DarkBackground
import com.example.alertapp.ui.theme.OnDarkBackground
import com.example.alertapp.ui.theme.OnDarkMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDetailScreen(
    alertId: String,
    onBack: () -> Unit,
    onOpenVideo: () -> Unit
) {
    val context = LocalContext.current
    var alert by remember { mutableStateOf<AlertItem?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(alertId) {
        if (alertId.isBlank()) {
            loading = false
            error = "Nieznany alert"
            return@LaunchedEffect
        }
        val id = alertId.toIntOrNull()
        if (id == null) {
            loading = false
            error = "Nieprawidłowe ID alertu"
            return@LaunchedEffect
        }
        loading = true
        error = null
        withContext(Dispatchers.IO) {
            try {
                val response = ApiProvider.getAlertApi(context).getAlert(id)
                if (response.isSuccessful) {
                    alert = response.body()
                    if (alert == null) error = "Brak danych alertu"
                } else {
                    error = "Błąd ładowania"
                }
            } catch (e: Exception) {
                error = e.message ?: "Błąd sieci"
            }
            loading = false
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły alertu", color = OnDarkBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wróć",
                            modifier = Modifier.size(28.dp),
                            tint = OnDarkBackground
                        )
                    }
                },
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
            if (loading) {
                CircularProgressIndicator(
                    Modifier.align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                val a = alert
                if (a != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface)
                    ) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            DetailRow(label = "ID", value = "#${a.id}")
                            DetailRow(
                                label = "Typ zagrożenia",
                                value = formatThreatTypeLabel(a.threat_type),
                                valueColor = AlertRed
                            )
                            DetailRow(
                                label = "Czas wykrycia",
                                value = formatDetectedAtLabel(a.detected_at)
                            )
                            DetailRow(
                                label = "Utworzono",
                                value = formatDetectedAtLabel(a.created_at)
                            )
                            if (a.video_path.isNotBlank()) {
                                Text(
                                    text = "Ścieżka wideo (serwer)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OnDarkMuted
                                )
                                Text(
                                    text = a.video_path,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnDarkMuted
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onOpenVideo,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Odtwórz wideo")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = OnDarkBackground
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = OnDarkMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = valueColor
        )
    }
}
