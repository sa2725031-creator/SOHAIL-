package com.example

import com.example.data.MeditationSession
import com.example.model.AnalyticsCalculator
import com.example.model.BreathingPattern
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testBreathingPatterns() {
    val box = BreathingPattern.BOX
    assertEquals(16, box.cycleDurationSec)
    assertEquals(4, box.inhaleSec)
    assertEquals(4, box.holdInhaleSec)
    assertEquals(4, box.exhaleSec)
    assertEquals(4, box.holdExhaleSec)
  }

  @Test
  fun testEmptyAnalytics() {
    val stats = AnalyticsCalculator.calculateStats(emptyList(), dailyGoalMin = 15)
    assertEquals(0, stats.todayMinutes)
    assertEquals(0, stats.totalMinutes)
    assertEquals(0, stats.totalSessions)
    assertEquals(0, stats.currentStreakDays)
    assertEquals(7, stats.weekDays.size)
  }

  @Test
  fun testSessionAnalyticsAggregation() {
    val now = System.currentTimeMillis()
    val sessions = listOf(
      MeditationSession(
        sessionTitle = "Mindfulness",
        sessionType = "Meditation",
        durationSeconds = 600, // 10 min
        timestamp = now
      ),
      MeditationSession(
        sessionTitle = "Box Breathing",
        sessionType = "Breathing",
        durationSeconds = 300, // 5 min
        timestamp = now
      )
    )

    val stats = AnalyticsCalculator.calculateStats(sessions, dailyGoalMin = 15)
    assertEquals(15, stats.todayMinutes)
    assertEquals(15, stats.totalMinutes)
    assertEquals(2, stats.totalSessions)
    assertEquals(1, stats.currentStreakDays)
  }
}
