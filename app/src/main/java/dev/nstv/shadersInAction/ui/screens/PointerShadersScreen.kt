package dev.nstv.shadersInAction.ui.screens

import android.graphics.RenderEffect
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import dev.nstv.shadersInAction.ui.shaders.Shaders

@Composable
fun PointerShadersScreen(modifier: Modifier = Modifier) {

    var touchPosition by remember { mutableStateOf(Offset.Zero) }
    var touchPositionDelta by remember { mutableStateOf(Offset.Zero) }

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
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                touchPositionDelta = offset - touchPosition
                                touchPosition = offset

                            }
                        )
                        detectDragGestures { change, dragAmount ->
                            touchPosition = change.position
                            touchPositionDelta = dragAmount
                        }
                    }
                    .graphicsLayer {
                        clip = true
                        shader.setFloatUniform("size", floatArrayOf(size.width, size.height))
                        shader.setFloatUniform("time", time)
                        shader.setFloatUniform(
                            "pointer",
                            floatArrayOf(touchPosition.x, touchPosition.y)
                        )
                        shader.setFloatUniform(
                            "pointerDelta",
                            floatArrayOf(
                                touchPositionDelta.x / size.width,
                                touchPositionDelta.y / size.height
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

