package com.example.alertapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey val id: Int,
    val threat_type: String,
    val detected_at: String,
    val video_path: String,
    val created_at: String,
    val cached_at_ms: Long
)

