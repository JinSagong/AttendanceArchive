package com.jin.attendance_archive.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp

private const val DefaultAnimationDuration = 500
private const val DefaultAnimationDelay = 130
private const val DefaultStartDelay = 0
private const val DefaultBallCount = 3

private val DefaultBallDiameterMax = 14.dp
private val DefaultSpacing = 4.dp
private val DefaultBallJumpHeight = 12.dp

@Composable
fun BallPulseSyncProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    animationDuration: Int = DefaultAnimationDuration,
    animationDelay: Int = DefaultAnimationDelay,
    startDelay: Int = DefaultStartDelay,
    ballCount: Int = DefaultBallCount,
    ballDiameter: Dp = DefaultBallDiameterMax,
    ballJumpHeight: Dp = DefaultBallJumpHeight,
    spacing: Dp = DefaultSpacing
) {
    val transition = rememberInfiniteTransition()

    val duration = startDelay + animationDuration + animationDelay

    //Fractional jump height
    val jumpHeight = arrayListOf<Float>().apply {
        for (i in 0 until ballCount) {
            val delay = startDelay + animationDelay / (ballCount - 1) * i
            val height by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = duration
                        0f at delay with FastOutSlowInEasing
                        1f at (animationDuration / 2) + delay with FastOutSlowInEasing
                        0f at animationDuration + delay
                        0f at duration
                    },
                )
            )
            add(height)
        }
    }

    val width = (ballDiameter + spacing) * ballCount - spacing
    val height = ballJumpHeight + ballDiameter

    ProgressIndicator(modifier, width, height) {
        drawIndeterminateBallPulseSyncIndicator(
            maxDiameter = ballDiameter.toPx(),
            jumpHeight = jumpHeight.map { lerp(0.dp, ballJumpHeight, it).toPx() },
            spacing = spacing.toPx(),
            color = color
        )
    }
}

private fun DrawScope.drawIndeterminateBallPulseSyncIndicator(
    maxDiameter: Float,
    jumpHeight: List<Float>,
    spacing: Float,
    color: Color
) {
    val y = size.height - (maxDiameter / 2)

    for (i in jumpHeight.indices) {
        val x = (maxDiameter + spacing) * i + maxDiameter / 2
        drawCircle(
            color = color,
            radius = maxDiameter / 2,
            center = Offset(x, y - jumpHeight[i])
        )
    }
}

@Composable
private fun ProgressIndicator(
    modifier: Modifier,
    size: Dp,
    onDraw: DrawScope.() -> Unit
) {
    Canvas(
        modifier = modifier
            .progressSemantics()
            .size(size)
            .focusable(),
        onDraw = onDraw
    )
}

@Composable
private fun ProgressIndicator(
    modifier: Modifier,
    width: Dp,
    height: Dp,
    onDraw: DrawScope.() -> Unit
) {
    Canvas(
        modifier = modifier
            .progressSemantics()
            .size(width, height)
            .focusable(),
        onDraw = onDraw
    )
}