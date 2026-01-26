package dev.nstv.shadersInAction.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ShaderBrush
import dev.nstv.shadersInAction.ui.components.BoxWithTime
import dev.nstv.shadersInAction.ui.components.ShadersWrapper
import dev.nstv.shadersInAction.ui.shaders.Shaders

@Composable
fun SimpleScreen(
    modifier: Modifier = Modifier
) {
    ShadersWrapper(
        modifier = modifier,
        shadersMap = Shaders.getStandaloneShaders()
    ) { shader ->
        BoxWithTime { time ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .drawWithCache {
                        val shaderBrush = ShaderBrush(shader)
                        shader.setFloatUniform("size", floatArrayOf(size.width, size.height))
                        shader.setFloatUniform("time", time)
                        onDrawBehind {
                            drawRect(shaderBrush)
                        }
                    }
            )
        }
    }
}
