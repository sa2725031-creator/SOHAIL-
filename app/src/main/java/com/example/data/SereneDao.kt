package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SereneDao {
    @Query("SELECT * FROM meditation_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<MeditationSession>>

    @Query("SELECT * FROM meditation_sessions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<MeditationSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: MeditationSession): Long

    @Delete
    suspend fun deleteSession(session: MeditationSession)

    @Query("DELETE FROM meditation_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("SELECT * FROM mindful_reflections ORDER BY timestamp DESC")
    fun getAllReflections(): Flow<List<MindfulReflection>>

    @Query("SELECT * FROM mindful_reflections WHERE dateKey = :dateKey LIMIT 1")
    fun getReflectionForDate(dateKey: String): Flow<MindfulReflection?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReflection(reflection: MindfulReflection): Long

    @Delete
    suspend fun deleteReflection(reflection: MindfulReflection)
}
