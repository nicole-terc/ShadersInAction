package dev.nstv.shadersInAction.ui.screens

import android.graphics.RenderEffect
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import dev.nstv.shadersInAction.R

@Composable
fun ImageScreen(modifier: Modifier = Modifier) {
    ShadersWrapper(modifier) { shader ->
        Image(
            bitmap = ImageBitmap.imageResource(R.drawable.sheep),
            contentDescription = "Sheep",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .graphicsLayer {
                    clip = true
                    shader.setFloatUniform("size", floatArrayOf(size.width, size.height))
                    renderEffect =
                        RenderEffect.createRuntimeShaderEffect(shader, "composable")
                            .asComposeRenderEffect()
                }
        )
    }
}
