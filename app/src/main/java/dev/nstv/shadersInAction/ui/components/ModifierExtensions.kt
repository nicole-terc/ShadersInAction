package dev.nstv.shadersInAction.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange

fun Modifier.dragAndTapDetection(
    onTap: (position: Offset) -> Unit,
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
    onDragEnd: () -> Unit = {},
    key: Any = Unit,
) = this then Modifier.pointerInput(key) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)

        var dragging = false

        val slop = awaitTouchSlopOrCancellation(down.id) { change, _ ->
            dragging = true
            change.consume() // start claiming events once we commit to drag
        }

        if (dragging && slop != null) {
            // Drag
            drag(slop.id) { change ->
                val dragAmount = change.positionChange()
                onDrag(change,dragAmount)
                change.consume()
            }
            onDragEnd()
        } else {
            // Tap (finger/mouse went up without exceeding slop)
            onTap(down.position)
        }
    }
}
