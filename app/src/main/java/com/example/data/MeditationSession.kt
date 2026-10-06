package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meditation_sessions")
data class MeditationSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionTitle: String,
    val sessionType: String, // "Meditation", "Breathing", "Body Scan", "Loving Kindness", "Sleep"
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val moodAfter: String = "Peaceful",
    val notes: String = "",
    val ambientSound: String = "Silent"
)
