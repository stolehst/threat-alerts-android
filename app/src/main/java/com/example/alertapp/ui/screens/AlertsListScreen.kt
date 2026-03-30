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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsListScreen(
    onAlertClick: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AlertsRepository(context.applicationContext) }
    var loading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }
    val pullRefreshState = rememberPullToRefreshState()

    // Filters
    var typeFilter by rememberSaveable { mutableStateOf<String?>(null) } // null = all
    var datePresetOrdinal by rememberSaveable { mutableStateOf(DatePreset.ALL.ordinal) }
    var query by rememberSaveable { mutableStateOf("") }
    var sortDescending by rememberSaveable { mutableStateOf(true) }
    val datePreset = remember(datePresetOrdinal) { DatePreset.entries[datePresetOrdinal] }

    val (fromIso, toIso) = remember(datePreset) { datePreset.toIsoRange() }

    val alerts: List<AlertItem> by repo
        .observeAlertsFiltered(
            threatType = typeFilter,
            fromIso = fromIso,
            toIso = toIso,
            query = query,
            sortDescending = sortDescending
        )
        .collectAsState(initial = emptyList())

    LaunchedEffect(refreshTrigger) {
        val isPullOrButton = refreshTrigger > 0
        if (isPullOrButton) {
            isRefreshing = true
        } else {
            loading = true
        }
        error = null
        // Refresh from network and update cache; UI reads from Room (Flow)
        try {
            withContext(Dispatchers.IO) { repo.refreshAlertsFromNetwork() }
        } catch (e: Exception) {
            error = e.message ?: "Błąd sieci"
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
            // Pull-to-refresh потребує прокручуваного контенту; інакше не працює при «Brak alertów» / помилці / завантаженні
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = if (alerts.isNotEmpty() && !loading && error == null) {
                    Arrangement.spacedBy(12.dp)
                } else {
                    Arrangement.spacedBy(0.dp)
                }
            ) {
                item {
                    FiltersPanel(
                        typeFilter = typeFilter,
                        onTypeFilterChange = { typeFilter = it },
                        datePreset = datePreset,
                        onDatePresetChange = { datePresetOrdinal = it.ordinal },
                        query = query,
                        onQueryChange = { query = it },
                        sortDescending = sortDescending,
                        onSortChange = { sortDescending = it }
                    )
                }
                when {
                    loading -> item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    error != null -> item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = error!!,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    alerts.isEmpty() -> item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Brak alertów",
                                style = MaterialTheme.typography.bodyLarge,
                                color = OnDarkMuted
                            )
                        }
                    }
                    else -> items(alerts, key = { it.id }) { alert ->
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

private enum class DatePreset(val label: String) {
    ALL("Wszystkie"),
    TODAY("Dzisiaj"),
    DAYS_7("7 dni"),
    DAYS_30("30 dni");

    fun toIsoRange(): Pair<String?, String?> {
        if (this == ALL) return null to null
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        // toIso = start of tomorrow
        val toCal = cal.clone() as Calendar
        toCal.add(Calendar.DAY_OF_YEAR, 1)
        val toIso = "${fmt.format(toCal.time)}T00:00:00"

        val fromCal = cal.clone() as Calendar
        when (this) {
            TODAY -> {
                // today start
            }
            DAYS_7 -> fromCal.add(Calendar.DAY_OF_YEAR, -6)
            DAYS_30 -> fromCal.add(Calendar.DAY_OF_YEAR, -29)
            ALL -> {}
        }
        val fromIso = "${fmt.format(fromCal.time)}T00:00:00"
        return fromIso to toIso
    }
}

@Composable
private fun FiltersPanel(
    typeFilter: String?,
    onTypeFilterChange: (String?) -> Unit,
    datePreset: DatePreset,
    onDatePresetChange: (DatePreset) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    sortDescending: Boolean,
    onSortChange: (Boolean) -> Unit
) {
    var typeMenu by remember { mutableStateOf(false) }
    var dateMenu by remember { mutableStateOf(false) }
    var sortMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Box {
                    FilledTonalButton(
                        onClick = { typeMenu = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        val selected = typeFilter?.replaceFirstChar { ch ->
                            if (ch.isLowerCase()) ch.uppercase() else ch.toString()
                        }
                        Text(if (selected.isNullOrBlank()) "Typ: Wszystkie" else "Typ: $selected")
                    }
                DropdownMenu(expanded = typeMenu, onDismissRequest = { typeMenu = false }) {
                    DropdownMenuItem(text = { Text("Wszystkie") }, onClick = {
                        onTypeFilterChange(null); typeMenu = false
                    })
                    listOf("fire", "fight", "smoke").forEach { t ->
                        val label = t.replaceFirstChar { ch ->
                            if (ch.isLowerCase()) ch.uppercase() else ch.toString()
                        }
                        DropdownMenuItem(text = { Text(label) }, onClick = {
                            onTypeFilterChange(t); typeMenu = false
                        })
                    }
                }
            }
                Box {
                    FilledTonalButton(
                        onClick = { dateMenu = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Data: ${datePreset.label}")
                    }
                DropdownMenu(expanded = dateMenu, onDismissRequest = { dateMenu = false }) {
                    DatePreset.entries.forEach { preset ->
                        DropdownMenuItem(text = { Text(preset.label) }, onClick = {
                            onDatePresetChange(preset); dateMenu = false
                        })
                    }
                }
            }
        }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Box {
                    OutlinedButton(
                        onClick = { sortMenu = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(if (sortDescending) "Sortowanie: nowe" else "Sortowanie: stare")
                    }
                DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                    DropdownMenuItem(text = { Text("Najnowsze") }, onClick = {
                        onSortChange(true); sortMenu = false
                    })
                    DropdownMenuItem(text = { Text("Najstarsze") }, onClick = {
                        onSortChange(false); sortMenu = false
                    })
                }
            }
        }

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Szukaj") },
                singleLine = true
            )
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

