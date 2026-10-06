package com.example.data

import kotlinx.coroutines.flow.Flow

class SereneRepository(private val dao: SereneDao) {
    val allSessions: Flow<List<MeditationSession>> = dao.getAllSessions()
    fun getRecentSessions(limit: Int = 5): Flow<List<MeditationSession>> = dao.getRecentSessions(limit)
    val allReflections: Flow<List<MindfulReflection>> = dao.getAllReflections()

    fun getReflectionForDate(dateKey: String): Flow<MindfulReflection?> = dao.getReflectionForDate(dateKey)

    suspend fun insertSession(session: MeditationSession): Long = dao.insertSession(session)

    suspend fun deleteSession(session: MeditationSession) = dao.deleteSession(session)

    suspend fun deleteSessionById(id: Long) = dao.deleteSessionById(id)

    suspend fun insertReflection(reflection: MindfulReflection): Long = dao.insertReflection(reflection)

    suspend fun deleteReflection(reflection: MindfulReflection) = dao.deleteReflection(reflection)
}
