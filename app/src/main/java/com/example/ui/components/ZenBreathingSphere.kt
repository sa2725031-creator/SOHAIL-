package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BreathPhase
import com.example.model.BreathingPattern
import com.example.ui.theme.BreathExhaleColor
import com.example.ui.theme.BreathHoldColor
import com.example.ui.theme.BreathInhaleColor
import com.example.ui.theme.WarmGlowAmber

@Composable
fun ZenBreathingSphere(
    isActive: Boolean,
    currentPhase: BreathPhase,
    secondsLeft: Int,
    pattern: BreathingPattern,
    modifier: Modifier = Modifier
) {
    // Animatable radius scale (0f = contracted, 1f = fully expanded)
    val scaleAnim = remember { Animatable(0.35f) }

    LaunchedEffect(isActive, currentPhase) {
        if (!isActive) {
            scaleAnim.animateTo(0.35f, tween(1000, easing = FastOutSlowInEasing))
        } else {
            when (currentPhase) {
                BreathPhase.PREPARE -> {
                    scaleAnim.animateTo(0.35f, tween(600, easing = FastOutSlowInEasing))
                }
                BreathPhase.INHALE -> {
                    val durationMs = (pattern.inhaleSec * 1000).coerceAtLeast(500)
                    scaleAnim.animateTo(1.0f, tween(durationMs, easing = FastOutSlowInEasing))
                }
                BreathPhase.HOLD_IN -> {
                    // Slight gentle micro-pulse while holding
                    scaleAnim.animateTo(0.98f, tween(500, easing = LinearEasing))
                }
                BreathPhase.EXHALE -> {
                    val durationMs = (pattern.exhaleSec * 1000).coerceAtLeast(500)
                    scaleAnim.animateTo(0.35f, tween(durationMs, easing = FastOutSlowInEasing))
                }
                BreathPhase.HOLD_OUT -> {
                    scaleAnim.animateTo(0.33f, tween(500, easing = LinearEasing))
                }
                BreathPhase.COMPLETE -> {
                    scaleAnim.animateTo(0.5f, tween(1200, easing = FastOutSlowInEasing))
                }
            }
        }
    }

    // Subtle gentle aura shimmer
    val infiniteTransition = rememberInfiniteTransition(label = "aura")
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraAlpha"
    )

    val currentPhaseColor = when (currentPhase) {
        BreathPhase.INHALE -> BreathInhaleColor
        BreathPhase.HOLD_IN -> BreathHoldColor
        BreathPhase.EXHALE -> BreathExhaleColor
        BreathPhase.HOLD_OUT -> BreathHoldColor
        BreathPhase.PREPARE -> MaterialTheme.colorScheme.primary
        BreathPhase.COMPLETE -> WarmGlowAmber
    }

    val phaseLabel = when (currentPhase) {
        BreathPhase.PREPARE -> "Get Ready"
        BreathPhase.INHALE -> "Breathe In"
        BreathPhase.HOLD_IN -> "Hold Breath"
        BreathPhase.EXHALE -> "Breathe Out"
        BreathPhase.HOLD_OUT -> "Rest"
        BreathPhase.COMPLETE -> "Peace"
    }

    Box(
        modifier = modifier
            .size(300.dp)
            .testTag("zen_breathing_sphere"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.width / 2.2f
            val minRadius = maxRadius * 0.42f
            val animatedRadius = minRadius + (maxRadius - minRadius) * scaleAnim.value

            // 1. Outer subtle guide ring
            drawCircle(
                color = currentPhaseColor.copy(alpha = 0.15f),
                radius = maxRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 2. Radiating soft energy ripple
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        currentPhaseColor.copy(alpha = auraAlpha * 0.5f),
                        currentPhaseColor.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = animatedRadius * 1.35f
                ),
                radius = animatedRadius * 1.35f,
                center = center
            )

            // 3. Main serene gradient sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        currentPhaseColor.copy(alpha = 0.9f),
                        currentPhaseColor.copy(alpha = 0.65f),
                        currentPhaseColor.copy(alpha = 0.35f)
                    ),
                    center = center.copy(y = center.y - animatedRadius * 0.15f),
                    radius = animatedRadius
                ),
                radius = animatedRadius,
                center = center
            )

            // 4. Delicate inner highlight glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f),
                        Color.Transparent
                    ),
                    center = center.copy(y = center.y - animatedRadius * 0.25f),
                    radius = animatedRadius * 0.65f
                ),
                radius = animatedRadius * 0.65f,
                center = center
            )
        }

        // Center Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.testTag("breathing_center_info")
        ) {
            Text(
                text = phaseLabel,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            if (isActive && secondsLeft > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${secondsLeft}s",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
