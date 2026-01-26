package dev.nstv.shadersInAction.ui.shaders

import org.intellij.lang.annotations.Language


@Language("AGSL")
const val SOLO_WHITE = """
    uniform float time;
    uniform float2 size;
    
    half4 main(float2 fragCoord) {
        return half4(1.0);
    }
"""

@Language("AGSL")
const val SOLO_RED = """
    uniform float time;
    uniform float2 size;
    
    half4 main(float2 fragCoord) {
        return half4(1.0, 0.0, 0.0, 1.0);
    } 
"""

@Language("AGSL")
const val SOLO_GREEN = """
    uniform float time;
    uniform float2 size;
    
    half4 main(float2 fragCoord) {
        return half4(0.0, 1.0, 0.0, 1.0);
    }
"""

@Language("AGSL")
const val SOLO_BLUE = """
    uniform float time;
    uniform float2 size;
    
    half4 main(float2 fragCoord) {
        return half4(0.0, 0.0, 1.0, 1.0);
    }
"""

@Language("AGSL")
const val SOLO_RGB_VERTICAL = """
    uniform float time;
    uniform float2 size;
    
    half4 main(float2 fragCoord) {
       float2 uv = fragCoord / size;
       
       if(uv.x <= 1.0/3.0){
            // RED tint
            return half4(1.0, 0.0, 0.0, 1.0);
       }
       if(uv.x <= 2.0/3.0){
            // GREEN tint
            return half4(0.0, 1.0, 0.0, 1.0);
       }
       
        // BLUE tint
        return half4(0.0, 0.0, 1.0, 1.0);
    }
"""

@Language("AGSL")
const val SOLO_RGB_HORIZONTAL = """
    uniform float time;
    uniform float2 size;
    
    half4 main(float2 fragCoord) {
       float2 uv = fragCoord / size;
       
       if(uv.y <= 1.0/3.0){
            // RED tint
            return half4(1.0, 0.0, 0.0, 1.0);
       }
       if(uv.y <= 2.0/3.0){
            // GREEN tint
            return half4(0.0, 1.0, 0.0, 1.0);
       }
       // BLUE tint
        return half4(0.0, 0.0, 1.0, 1.0);
    }
"""

