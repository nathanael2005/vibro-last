package com.nate.tv.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.nate.core.common.theme.ThemeTokens

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvCircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFFC107),
    strokeWidth: Dp = 3.dp
) {
    val transition = rememberInfiniteTransition(label = "cinematic_loading")

    // Counter-rotating orbital angles
    val outerAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing)
        ),
        label = "outer_angle"
    )

    val innerAngle by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing)
        ),
        label = "inner_angle"
    )

    // Breathing pulse scale
    val pulseScale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Breathing alpha for text
    val textAlpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "text_alpha"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(68.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer Gold Orbital Ring
            Canvas(modifier = Modifier.size(64.dp)) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFFFFC107).copy(alpha = 0.1f),
                            Color(0xFFFFD54F),
                            Color(0xFFFFC107)
                        )
                    ),
                    startAngle = outerAngle,
                    sweepAngle = 240f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }

            // Inner Cyan Accent Ring
            Canvas(modifier = Modifier.size(48.dp)) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = 0.2f),
                            Color(0xFF00E5FF)
                        )
                    ),
                    startAngle = innerAngle,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(width = (strokeWidth - 1.dp).coerceAtLeast(2.dp).toPx(), cap = StrokeCap.Round)
                )
            }

            // Center Glowing NATI Emblem
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(Color(0xFF0D1B2A))
                    .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "N",
                    color = Color(0xFFFFC107),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "NATI TV",
            color = ThemeTokens.TextWhite.copy(alpha = textAlpha),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp
        )
    }
}
