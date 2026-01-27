package dev.nstv.shadersInAction.ui.screens

import android.graphics.RuntimeShader
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ShaderBrush
import dev.nstv.shadersInAction.ui.shaders.SOLO_RB_VERTICAL

const val SHADER = """
    uniform float time;
    uniform float2 size;
    
    half4 main(float2 fragCoord) {
        if(fragCoord.x <= size.x / 2.0){
            // RED tint
            return half4(1.0, 0.0, 0.0, 1.0);
        }

        // BLUE tint
        return half4(0.0, 0.0, 1.0, 1.0);
    }
"""

val shader = RuntimeShader(SHADER)

@Composable
fun DemoScreen() {
    val shaderBrush = ShaderBrush(shader)
    Box(
        Modifier
            .fillMaxSize()
            .drawWithCache {
                shader.setFloatUniform("time", 0f)
                shader.setFloatUniform("size", floatArrayOf(size.width, size.height))
                onDrawBehind {
                    drawRect(shaderBrush)
                }
            }
    )
}
