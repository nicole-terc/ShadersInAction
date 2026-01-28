package dev.nstv.shadersInAction.ui.components

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import dev.nstv.shadersInAction.ui.DefaultSpeed

// Source: https://github.com/drinkthestars/shady/blob/main/sketch/src/main/kotlin/com/goofy/goober/sketch/Sketch.kt
@Composable
fun produceDrawLoopCounter(speed: Float = 1f): State<Float> {
    return produceState(0f) {
        val firstFrame: Long = withFrameMillis { it }
        while (true) {
            withInfiniteAnimationFrameMillis {
                value = (it - firstFrame) * speed / 1000f
            }
        }
    }
}

@Composable
fun BoxWithTime(
    modifier: Modifier = Modifier,
    speed: Float = DefaultSpeed,
    content: @Composable (time: Float) -> Unit,
) {
    val time by produceDrawLoopCounter(speed)
    Box(modifier = modifier) {
        content(time)
    }
}
