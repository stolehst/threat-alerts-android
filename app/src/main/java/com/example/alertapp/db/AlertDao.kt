package com.example.alertapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {

    @Query("SELECT * FROM alerts ORDER BY detected_at DESC")
    suspend fun getAll(): List<AlertEntity>

    @Query(
        """
        SELECT * FROM alerts
        WHERE (:threatType IS NULL OR threat_type LIKE '%' || :threatType || '%')
          AND (:fromIso IS NULL OR detected_at >= :fromIso)
          AND (:toIso IS NULL OR detected_at < :toIso)
          AND (:q IS NULL OR (threat_type LIKE '%' || :q || '%' OR detected_at LIKE '%' || :q || '%'))
        ORDER BY detected_at DESC
        """
    )
    fun observeFilteredDesc(
        threatType: String?,
        fromIso: String?,
        toIso: String?,
        q: String?
    ): Flow<List<AlertEntity>>

    @Query(
        """
        SELECT * FROM alerts
        WHERE (:threatType IS NULL OR threat_type LIKE '%' || :threatType || '%')
          AND (:fromIso IS NULL OR detected_at >= :fromIso)
          AND (:toIso IS NULL OR detected_at < :toIso)
          AND (:q IS NULL OR (threat_type LIKE '%' || :q || '%' OR detected_at LIKE '%' || :q || '%'))
        ORDER BY detected_at ASC
        """
    )
    fun observeFilteredAsc(
        threatType: String?,
        fromIso: String?,
        toIso: String?,
        q: String?
    ): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): AlertEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<AlertEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: AlertEntity)

    @Query("DELETE FROM alerts")
    suspend fun clear()
}

