package com.example.model

data class MindfulQuote(
    val quote: String,
    val author: String,
    val practiceTheme: String
)

object MindfulWisdom {
    val quotes = listOf(
        MindfulQuote(
            "Smile, breathe and go slowly.",
            "Thich Nhat Hanh",
            "Present Moment"
        ),
        MindfulQuote(
            "You cannot stop the waves, but you can learn to surf.",
            "Jon Kabat-Zinn",
            "Acceptance"
        ),
        MindfulQuote(
            "In today's rush, we all think too much, seek too much, want too much and forget about the joy of just being.",
            "Eckhart Tolle",
            "Stillness"
        ),
        MindfulQuote(
            "Feelings come and go like clouds in a windy sky. Conscious breathing is my anchor.",
            "Thich Nhat Hanh",
            "Anchor"
        ),
        MindfulQuote(
            "The soul should always stand ajar, ready to welcome the ecstatic experience.",
            "Emily Dickinson",
            "Openness"
        ),
        MindfulQuote(
            "Do not dwell in the past, do not dream of the future, concentrate the mind on the present moment.",
            "Buddha",
            "Mindfulness"
        ),
        MindfulQuote(
            "Within you, there is a stillness and a sanctuary to which you can retreat at any time.",
            "Hermann Hesse",
            "Sanctuary"
        ),
        MindfulQuote(
            "Quiet the mind, and the soul will speak.",
            "Ma Jaya Sati Bhagavati",
            "Inner Voice"
        ),
        MindfulQuote(
            "The quieter you become, the more you are able to hear.",
            "Rumi",
            "Listening"
        ),
        MindfulQuote(
            "Peace comes from within. Do not seek it without.",
            "Siddhartha Gautama",
            "Inner Peace"
        ),
        MindfulQuote(
            "Nothing can bring you peace but yourself.",
            "Ralph Waldo Emerson",
            "Self-Compassion"
        ),
        MindfulQuote(
            "Surrender to what is. Say 'yes' to life — and see how life suddenly starts working for you rather than against you.",
            "Eckhart Tolle",
            "Surrender"
        )
    )

    fun getQuoteForDay(dayOfYear: Int): MindfulQuote {
        val index = (dayOfYear.coerceAtLeast(0)) % quotes.size
        return quotes[index]
    }
}

enum class BreathingPattern(
    val title: String,
    val subtitle: String,
    val inhaleSec: Int,
    val holdInhaleSec: Int,
    val exhaleSec: Int,
    val holdExhaleSec: Int,
    val recommendedRounds: Int
) {
    BOX(
        title = "Box Breathing",
        subtitle = "Navy SEAL technique for instant calm & nervous system reset",
        inhaleSec = 4,
        holdInhaleSec = 4,
        exhaleSec = 4,
        holdExhaleSec = 4,
        recommendedRounds = 4
    ),
    RELAX_478(
        title = "4-7-8 Tranquility",
        subtitle = "Dr. Weil technique for deep anxiety release and sound sleep",
        inhaleSec = 4,
        holdInhaleSec = 7,
        exhaleSec = 8,
        holdExhaleSec = 0,
        recommendedRounds = 4
    ),
    EQUAL_BREATH(
        title = "Equal Breathing (Sama)",
        subtitle = "Balance energy, stabilize autonomic rhythm, sharpen clarity",
        inhaleSec = 5,
        holdInhaleSec = 0,
        exhaleSec = 5,
        holdExhaleSec = 0,
        recommendedRounds = 6
    ),
    AWAKEN_DEEP(
        title = "Awaken & Energize",
        subtitle = "Quick oxygen replenishment for midday sluggishness",
        inhaleSec = 3,
        holdInhaleSec = 1,
        exhaleSec = 5,
        holdExhaleSec = 0,
        recommendedRounds = 5
    );

    val cycleDurationSec: Int
        get() = inhaleSec + holdInhaleSec + exhaleSec + holdExhaleSec
}

enum class BreathPhase(val label: String, val instruction: String) {
    PREPARE("Prepare", "Settle into your posture and soften your gaze"),
    INHALE("Inhale", "Breathe in deeply through your nose"),
    HOLD_IN("Hold", "Gently retain the breath with relaxed shoulders"),
    EXHALE("Exhale", "Release slowly and completely through mouth"),
    HOLD_OUT("Sustain", "Rest in the empty stillness"),
    COMPLETE("Completed", "Notice the tranquil space inside you")
}

data class DailyMindfulDayStat(
    val dateLabel: String, // e.g. "Mon", "Tue"
    val dateKey: String,   // "yyyy-MM-dd"
    val minutesMeditated: Int,
    val isToday: Boolean
)
