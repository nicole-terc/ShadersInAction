package dev.nstv.shadersInAction.ui.shaders

import android.graphics.RuntimeShader


object SimpleShaders {
    private val red = RuntimeShader(RED)
    private val green = RuntimeShader(GREEN)
    private val blue = RuntimeShader(BLUE)

    fun getShaderMap() = mapOf(
        "red" to red,
        "green" to green,
        "blue" to blue,
    )
}

const val RED = """
    uniform shader composable;
    
    half4 main(float2 fragCoord) {
        half4 color = composable.eval(fragCoord).rgba;
        return half4(1.0, color.gba);
    } 
"""

const val GREEN = """
    uniform shader composable;
    
    half4 main(float2 fragCoord) {
        half4 color = composable.eval(fragCoord).rgba;
        return half4(color.r, 1.0, color.ba);
    }
"""

const val BLUE = """
    uniform shader composable;
    
    half4 main(float2 fragCoord) {
        half4 color = composable.eval(fragCoord).rgba;
        return half4(color.rg, 1.0, color.a);
    }
"""



