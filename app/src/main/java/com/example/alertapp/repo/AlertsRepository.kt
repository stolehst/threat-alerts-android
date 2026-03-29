package com.example.alertapp.repo

import android.content.Context
import com.example.alertapp.api.AlertItem
import com.example.alertapp.api.ApiProvider
import com.example.alertapp.db.AlertEntity
import com.example.alertapp.db.AppDatabase

class AlertsRepository(private val appContext: Context) {

    // Defer Room initialization until first use (and make sure it's called from IO in callers)
    private val dao by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        AppDatabase.get(appContext).alertDao()
    }
    private val api by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        ApiProvider.getAlertApi(appContext)
    }

    suspend fun getCachedAlerts(): List<AlertItem> =
        dao.getAll().map { it.toApiModel() }

    suspend fun refreshAlertsFromNetwork(): List<AlertItem> {
        val resp = api.getAlerts()
        if (!resp.isSuccessful) throw RuntimeException("HTTP ${resp.code()}")
        val alerts = resp.body()?.alerts ?: emptyList()
        val now = System.currentTimeMillis()
        dao.upsertAll(alerts.map { it.toEntity(now) })
        return alerts
    }

    suspend fun getCachedAlert(id: Int): AlertItem? =
        dao.getById(id)?.toApiModel()

    suspend fun refreshAlertFromNetwork(id: Int): AlertItem {
        val resp = api.getAlert(id)
        if (!resp.isSuccessful) throw RuntimeException("HTTP ${resp.code()}")
        val alert = resp.body() ?: throw RuntimeException("Empty body")
        dao.upsert(alert.toEntity(System.currentTimeMillis()))
        return alert
    }
}

private fun AlertEntity.toApiModel(): AlertItem =
    AlertItem(
        id = id,
        threat_type = threat_type,
        detected_at = detected_at,
        video_path = video_path,
        created_at = created_at
    )

private fun AlertItem.toEntity(cachedAtMs: Long): AlertEntity =
    AlertEntity(
        id = id,
        threat_type = threat_type,
        detected_at = detected_at,
        video_path = video_path,
        created_at = created_at,
        cached_at_ms = cachedAtMs
    )

