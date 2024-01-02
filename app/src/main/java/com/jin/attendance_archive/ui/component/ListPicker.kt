package com.jin.attendance_archive.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun <T> ListPicker(
    modifier: Modifier = Modifier,
    label: (T) -> String = { it.toString() },
    value: T,
    onValueChange: (T) -> Unit,
    dividersColor: Color = MaterialTheme.colorScheme.onSurface,
    list: List<T>,
    textStyle: TextStyle = LocalTextStyle.current,
    lastIdxForInvalidValue: Boolean = false
) {
    val minimumAlpha = 0.3f
    val verticalMargin = 8.dp
    val numbersColumnHeight = 80.dp
    val halfNumbersColumnHeight = numbersColumnHeight / 2
    val halfNumbersColumnHeightPx = with(LocalDensity.current) { halfNumbersColumnHeight.toPx() }

    val coroutineScope = rememberCoroutineScope()

    val animatedOffset = remember { Animatable(0f) }
        .apply {
            val index = list.indexOf(value).let {
                if (it == -1) {
                    if (lastIdxForInvalidValue) list.lastIndex else 0
                } else {
                    it
                }
            }
            val offsetRange = remember(value, list) {
                -((list.lastIndex) - index) * halfNumbersColumnHeightPx to
                        index * halfNumbersColumnHeightPx
            }
            updateBounds(offsetRange.first, offsetRange.second)
        }

    val indexOfElement = getItemIndexForOffset(
        list,
        value,
        animatedOffset.value,
        halfNumbersColumnHeightPx,
        lastIdxForInvalidValue
    )
    val coercedAnimatedOffset =
        getItemCoercedOffset(animatedOffset.value, halfNumbersColumnHeightPx)
    var prevIndex by remember { mutableStateOf(0) }
    var onDrag by remember { mutableStateOf(false) }

    var dividersWidth by remember { mutableStateOf(0.dp) }

    SideEffect {
        coroutineScope.launch {
            if (indexOfElement != prevIndex) {
                animatedOffset.snapTo(if (onDrag) coercedAnimatedOffset else 0f)
                prevIndex = indexOfElement
                onValueChange(list.elementAt(indexOfElement))
            }
        }
    }

    Layout(
        modifier = modifier
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { deltaY ->
                    coroutineScope.launch {
                        animatedOffset.snapTo(animatedOffset.value + deltaY)
                    }
                },
                onDragStarted = { onDrag = true },
                onDragStopped = { velocity ->
                    coroutineScope.launch {
                        animatedOffset.fling(
                            initialVelocity = velocity,
                            animationSpec = exponentialDecay(frictionMultiplier = 20f),
                            adjustTarget = { target ->
                                val coercedTarget = target % halfNumbersColumnHeightPx
                                val coercedAnchors =
                                    listOf(
                                        -halfNumbersColumnHeightPx,
                                        0f,
                                        halfNumbersColumnHeightPx
                                    )
                                val coercedPoint =
                                    coercedAnchors.minByOrNull { abs(it - coercedTarget) }!!
                                val base =
                                    halfNumbersColumnHeightPx * (target / halfNumbersColumnHeightPx).toInt()
                                coercedPoint + base
                            }
                        )
                    }
                    onDrag = false
                }
            ),
        content = {
            Box(
                Modifier
                    .width(dividersWidth)
                    .height(numbersColumnHeight / 3 + verticalMargin * 2)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            coroutineScope.launch {
                                animatedOffset.animateTo(animatedOffset.value + halfNumbersColumnHeightPx)
                            }
                        })
                    }
            )
            Box(
                Modifier
                    .width(dividersWidth)
                    .height(2.dp)
                    .background(color = dividersColor)
            )
            Box(
                modifier = Modifier
                    .padding(vertical = verticalMargin, horizontal = 20.dp)
                    .offset { IntOffset(x = 0, y = coercedAnimatedOffset.roundToInt()) }
            ) {
                val baseLabelModifier = Modifier.align(Alignment.Center)
                ProvideTextStyle(textStyle) {
                    if (indexOfElement > 0) Label(
                        text = label(list.elementAt(indexOfElement - 1)),
                        modifier = baseLabelModifier
                            .offset(y = -halfNumbersColumnHeight)
                            .alpha(
                                maxOf(
                                    minimumAlpha,
                                    coercedAnimatedOffset / halfNumbersColumnHeightPx
                                )
                            )
                    )
                    Label(
                        text = label(list.elementAt(indexOfElement)),
                        modifier = baseLabelModifier
                            .alpha(
                                (maxOf(
                                    minimumAlpha,
                                    1 - abs(coercedAnimatedOffset) / halfNumbersColumnHeightPx
                                ))
                            )
                    )
                    if (indexOfElement < list.size - 1) Label(
                        text = label(list.elementAt(indexOfElement + 1)),
                        modifier = baseLabelModifier
                            .offset(y = halfNumbersColumnHeight)
                            .alpha(
                                maxOf(
                                    minimumAlpha,
                                    -coercedAnimatedOffset / halfNumbersColumnHeightPx
                                )
                            )
                    )
                }
            }
            Box(
                Modifier
                    .width(dividersWidth)
                    .height(2.dp)
                    .background(color = dividersColor)
            )
            Box(
                Modifier
                    .width(dividersWidth)
                    .height(numbersColumnHeight / 3 + verticalMargin * 2)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            coroutineScope.launch {
                                animatedOffset.animateTo(animatedOffset.value - halfNumbersColumnHeightPx)
                            }
                        })
                    }
            )
        }
    ) { measurables, constraints ->
        // Don't constrain child views further, measure them with given constraints
        // List of measured children
        val placeables = measurables.map { measurable ->
            // Measure each children
            measurable.measure(constraints)
        }
        dividersWidth = placeables[2].width.toDp()

        // Set the size of the layout as big as it can
        layout(dividersWidth.toPx().toInt(), placeables.sumOf { it.height }) {
            // Track the y co-ord we have placed children up to
            var yPosition = 0

            // Place children in the parent layout
            placeables.forEach { placeable ->
                // Position item on the screen
                placeable.placeRelative(x = 0, y = yPosition)

                // Record the y co-ord placed up to
                yPosition += placeable.height
            }
        }
    }
}

@Composable
private fun Label(text: String, modifier: Modifier) {
    Text(
        modifier = modifier,
        text = text,
        textAlign = TextAlign.Center,
        fontSize = 14f.sp
    )
}

private fun <T> getItemIndexForOffset(
    range: List<T>,
    value: T,
    offset: Float,
    halfNumbersColumnHeightPx: Float,
    lastIdxForInvalidValue: Boolean
): Int {
    val indexDelta =
        ((offset / halfNumbersColumnHeightPx * 2 + if (offset >= 0) 1 else -1) / 2).toInt()
    val indexOf = range.indexOf(value).let {
        if (it == -1) {
            if (lastIdxForInvalidValue) range.lastIndex else 0
        } else {
            it
        }
    } - indexDelta
    return maxOf(0, minOf(indexOf, range.size - 1))
}

private fun getItemCoercedOffset(offset: Float, halfNumbersColumnHeightPx: Float): Float {
    val indexDelta =
        ((offset / halfNumbersColumnHeightPx * 2 + if (offset >= 0) 1 else -1) / 2).toInt()
    return offset - halfNumbersColumnHeightPx * indexDelta
}

private suspend fun Animatable<Float, AnimationVector1D>.fling(
    initialVelocity: Float,
    animationSpec: DecayAnimationSpec<Float>,
    adjustTarget: ((Float) -> Float)?,
    block: (Animatable<Float, AnimationVector1D>.() -> Unit)? = null,
): AnimationResult<Float, AnimationVector1D> {
    val targetValue = animationSpec.calculateTargetValue(value, initialVelocity)
    val adjustedTarget = adjustTarget?.invoke(targetValue)
    return if (adjustedTarget != null) {
        animateTo(
            targetValue = adjustedTarget,
            initialVelocity = initialVelocity,
            block = block
        )
    } else {
        animateDecay(
            initialVelocity = initialVelocity,
            animationSpec = animationSpec,
            block = block,
        )
    }
}