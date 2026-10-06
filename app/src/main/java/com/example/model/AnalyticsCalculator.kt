package com.example.model

import com.example.data.MeditationSession
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class UserMindfulStats(
    val todayMinutes: Int,
    val totalMinutes: Int,
    val totalSessions: Int,
    val currentStreakDays: Int,
    val longestStreakDays: Int,
    val weekDays: List<DailyMindfulDayStat>,
    val averageSessionMinutes: Int,
    val favoriteType: String
)

object AnalyticsCalculator {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dayLabelFormat = SimpleDateFormat("EEE", Locale.getDefault())

    fun calculateStats(sessions: List<MeditationSession>, dailyGoalMin: Int): UserMindfulStats {
        val todayCalendar = Calendar.getInstance()
        val todayKey = dateFormat.format(todayCalendar.time)

        // Sum minutes per date
        val minutesByDate = mutableMapOf<String, Int>()
        val countByType = mutableMapOf<String, Int>()
        var totalSec = 0

        for (session in sessions) {
            val sessionDate = dateFormat.format(Date(session.timestamp))
            val mins = session.durationSeconds / 60
            minutesByDate[sessionDate] = (minutesByDate[sessionDate] ?: 0) + mins
            countByType[session.sessionTitle] = (countByType[session.sessionTitle] ?: 0) + 1
            totalSec += session.durationSeconds
        }

        val todayMinutes = minutesByDate[todayKey] ?: 0
        val totalMinutes = totalSec / 60
        val totalSessions = sessions.size
        val avgMins = if (totalSessions > 0) totalMinutes / totalSessions else 0
        val favoriteType = countByType.maxByOrNull { it.value }?.key ?: "Mindfulness"

        // Compute current streak and longest streak
        val (currentStreak, longestStreak) = computeStreaks(minutesByDate.keys)

        // Compute last 7 days
        val weekList = mutableListOf<DailyMindfulDayStat>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)

        for (i in 0..6) {
            val dateKey = dateFormat.format(cal.time)
            val label = dayLabelFormat.format(cal.time)
            val mins = minutesByDate[dateKey] ?: 0
            val isToday = (dateKey == todayKey)
            weekList.add(
                DailyMindfulDayStat(
                    dateLabel = label,
                    dateKey = dateKey,
                    minutesMeditated = mins,
                    isToday = isToday
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return UserMindfulStats(
            todayMinutes = todayMinutes,
            totalMinutes = totalMinutes,
            totalSessions = totalSessions,
            currentStreakDays = currentStreak,
            longestStreakDays = longestStreak,
            weekDays = weekList,
            averageSessionMinutes = avgMins,
            favoriteType = favoriteType
        )
    }

    private fun computeStreaks(activeDates: Set<String>): Pair<Int, Int> {
        if (activeDates.isEmpty()) return Pair(0, 0)

        val cal = Calendar.getInstance()
        val todayKey = dateFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayKey = dateFormat.format(cal.time)

        // Current streak
        var currentStreak = 0
        var checkCal = Calendar.getInstance()

        if (activeDates.contains(todayKey)) {
            currentStreak = 1
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
            while (activeDates.contains(dateFormat.format(checkCal.time))) {
                currentStreak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            }
        } else if (activeDates.contains(yesterdayKey)) {
            currentStreak = 1
            checkCal.add(Calendar.DAY_OF_YEAR, -2)
            while (activeDates.contains(dateFormat.format(checkCal.time))) {
                currentStreak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            }
        }

        // Longest streak
        val sortedDates = activeDates.sorted()
        var maxStreak = if (activeDates.isNotEmpty()) 1 else 0
        var tempStreak = 1

        for (i in 1 until sortedDates.size) {
            try {
                val d1 = dateFormat.parse(sortedDates[i - 1])
                val d2 = dateFormat.parse(sortedDates[i])
                if (d1 != null && d2 != null) {
                    val diffDays = (d2.time - d1.time) / (1000 * 60 * 60 * 24)
                    if (diffDays == 1L) {
                        tempStreak++
                        if (tempStreak > maxStreak) maxStreak = tempStreak
                    } else if (diffDays > 1L) {
                        tempStreak = 1
                    }
                }
            } catch (_: Exception) {}
        }

        if (currentStreak > maxStreak) {
            maxStreak = currentStreak
        }

        return Pair(currentStreak, maxStreak)
    }
}
