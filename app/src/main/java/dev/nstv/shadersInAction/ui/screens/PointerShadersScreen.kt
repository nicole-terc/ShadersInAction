package dev.nstv.shadersInAction.ui.screens

import android.graphics.RenderEffect
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import dev.nstv.shadersInAction.R
import dev.nstv.shadersInAction.ui.components.BoxWithTime
import dev.nstv.shadersInAction.ui.components.ShadersWrapper
import dev.nstv.shadersInAction.ui.components.dragAndTapDetection
import dev.nstv.shadersInAction.ui.shaders.Shaders
import kotlinx.coroutines.launch

@Composable
fun PointerShadersScreen(modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    var touchPosition = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var touchPositionDelta = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    ShadersWrapper(
        shadersMap = Shaders.getShadersWithPointer(),
        modifier = modifier,
    ) { shader ->
        BoxWithTime { time ->
            Image(
                bitmap = ImageBitmap.imageResource(R.drawable.sheep),
                contentDescription = "Sheep",
                contentScale = ContentScale.Crop,
            )
            Image(
                bitmap = ImageBitmap.imageResource(R.drawable.sheep),
                contentDescription = "Sheep",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .dragAndTapDetection(
                        onTap = { offset ->
                            coroutineScope.launch {
                                touchPositionDelta.snapTo(Offset.Zero)
                                touchPosition.snapTo(offset)
                            }
                        },
                        onDrag = { change, dragAmount ->
                            coroutineScope.launch {
                                touchPosition.snapTo(change.position)
                                touchPositionDelta.snapTo(dragAmount)
                            }
                        }
                    )
                    .graphicsLayer {
                        clip = true
                        shader.setFloatUniform("size", floatArrayOf(size.width, size.height))
                        shader.setFloatUniform("time", time)
                        shader.setFloatUniform(
                            "pointer",
                            floatArrayOf(touchPosition.value.x, touchPosition.value.y)
                        )
                        shader.setFloatUniform(
                            "pointerDelta",
                            floatArrayOf(
                                touchPositionDelta.value.x / size.width,
                                touchPositionDelta.value.y / size.height
                            )
                        )

                        renderEffect =
                            RenderEffect.createRuntimeShaderEffect(shader, "composable")
                                .asComposeRenderEffect()
                    }
            )
        }
    }
}

