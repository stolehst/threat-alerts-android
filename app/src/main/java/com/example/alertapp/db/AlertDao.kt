package com.example.alertapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AlertDao {

    @Query("SELECT * FROM alerts ORDER BY detected_at DESC")
    suspend fun getAll(): List<AlertEntity>

    @Query("SELECT * FROM alerts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): AlertEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<AlertEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: AlertEntity)

    @Query("DELETE FROM alerts")
    suspend fun clear()
}

