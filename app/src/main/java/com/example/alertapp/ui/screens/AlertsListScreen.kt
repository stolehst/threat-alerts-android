package com.example.alertapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.alertapp.R
import com.example.alertapp.api.AlertItem
import com.example.alertapp.repo.AlertsRepository
import com.example.alertapp.ui.theme.AlertRed
import com.example.alertapp.ui.theme.CardSurface
import com.example.alertapp.ui.theme.DarkBackground
import com.example.alertapp.ui.theme.OnDarkBackground
import com.example.alertapp.ui.formatDetectedAtLabel
import com.example.alertapp.ui.formatThreatTypeLabel
import com.example.alertapp.ui.theme.OnDarkMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsListScreen(
    onAlertClick: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AlertsRepository(context.applicationContext) }
    var alerts by remember { mutableStateOf<List<AlertItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(refreshTrigger) {
        val isPullOrButton = refreshTrigger > 0
        if (isPullOrButton) {
            isRefreshing = true
        } else {
            loading = true
        }
        error = null
        // 1) Show cached data immediately (if any)
        val cached = withContext(Dispatchers.IO) { repo.getCachedAlerts() }
        if (cached.isNotEmpty()) alerts = cached

        // 2) Refresh from network and update cache
        try {
            val fresh = withContext(Dispatchers.IO) { repo.refreshAlertsFromNetwork() }
            // Не затирати список порожньою відповіддю після pull-to-refresh (часта причина «все зникло»).
            if (fresh.isNotEmpty() || alerts.isEmpty()) {
                alerts = fresh
            }
        } catch (e: Exception) {
            if (alerts.isEmpty()) {
                error = e.message ?: "Błąd sieci"
            }
        } finally {
            loading = false
            isRefreshing = false
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_name),
                        color = OnDarkBackground,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { /* поглинаємо тап, щоб не потрапляв у контент/pull під шапкою */ }
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardSurface,
                    titleContentColor = OnDarkBackground
                ),
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Text("⚙", style = MaterialTheme.typography.titleLarge, color = OnDarkBackground)
                    }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { refreshTrigger++ },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DarkBackground)
        ) {
            when {
                loading -> CircularProgressIndicator(
                    Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
                error != null -> Text(
                    text = error!!,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
                alerts.isEmpty() -> Text(
                    text = "Brak alertów",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnDarkMuted
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alerts, key = { it.id }) { alert ->
                        AlertListItem(
                            alert = alert,
                            onClick = { onAlertClick(alert.id.toString()) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertListItem(alert: AlertItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatDetectedAtLabel(alert.detected_at),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnDarkMuted
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AlertRed.copy(alpha = 0.2f),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .padding(start = 10.dp, top = 6.dp, bottom = 6.dp)
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(AlertRed)
                        )
                        Text(
                            text = formatThreatTypeLabel(alert.threat_type),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = AlertRed
                        )
                    }
                }
            }
        }
    }
}

