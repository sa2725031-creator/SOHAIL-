package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mindful_reflections")
data class MindfulReflection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateKey: String, // e.g. "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis(),
    val mood: String, // "Peaceful", "Joyful", "Calm", "Reflective", "Anxious", "Fatigued"
    val gratitudeNote: String,
    val intentionText: String
)
