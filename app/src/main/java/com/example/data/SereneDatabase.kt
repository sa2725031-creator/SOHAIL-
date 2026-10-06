package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MeditationSession::class, MindfulReflection::class],
    version = 1,
    exportSchema = false
)
abstract class SereneDatabase : RoomDatabase() {
    abstract fun sereneDao(): SereneDao

    companion object {
        @Volatile
        private var INSTANCE: SereneDatabase? = null

        fun getDatabase(context: Context): SereneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SereneDatabase::class.java,
                    "serene_mindfulness_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
