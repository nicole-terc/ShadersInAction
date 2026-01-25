package dev.nstv.shadersInAction.ui.shaders


const val NONE = """
    uniform shader composable;
    half4 main(float2 fragCoord) {
        return composable.eval(fragCoord);
    }
"""

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

// Source: https://www.youtube.com/watch?v=hjJesq71UXc
const val CHROMATIC_ABERRATION = """
    uniform shader composable;
    uniform float distortion;

    half4 main(float2 fragCoord) {
        half4 color = composable.eval(fragCoord).rgba;
        color.r = composable.eval(fragCoord + float2(distortion, 0)).r;
        color.b = composable.eval(fragCoord - float2(distortion, 0)).b;
        return half4(color);
    }
"""



