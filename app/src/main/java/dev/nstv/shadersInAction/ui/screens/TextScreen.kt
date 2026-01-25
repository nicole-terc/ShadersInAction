package dev.nstv.shadersInAction.ui.screens

import android.graphics.RenderEffect
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer


@Composable
fun TextScreen(
    modifier: Modifier = Modifier
) {
    ShadersWrapper(modifier) { shader ->
        Text(
            text = "Screen A",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    clip = true
                    shader.setFloatUniform("size", floatArrayOf(size.width, size.height))
                    renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "composable")
                        .asComposeRenderEffect()
                }
        )
    }
}
