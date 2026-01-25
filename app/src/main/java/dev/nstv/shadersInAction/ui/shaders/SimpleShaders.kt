package dev.nstv.shadersInAction.ui.shaders

import org.intellij.lang.annotations.Language

@Language("AGSL")
const val NONE = """
    uniform float time;
    uniform float2 size;
    uniform shader composable;
    
    half4 main(float2 fragCoord) {
        return composable.eval(fragCoord);
    }
"""

@Language("AGSL")
const val RED = """
    uniform float2 size;
    uniform shader composable;
    
    half4 main(float2 fragCoord) {
        half4 color = composable.eval(fragCoord).rgba;
        return half4(1.0, color.gba);
    } 
"""

@Language("AGSL")
const val GREEN = """
    uniform float2 size;
    uniform shader composable;
    
    half4 main(float2 fragCoord) {
        half4 color = composable.eval(fragCoord).rgba;
        return half4(color.r, 1.0, color.ba);
    }
"""

@Language("AGSL")
const val BLUE = """
    uniform float2 size;
    uniform shader composable;
    
    half4 main(float2 fragCoord) {
        half4 color = composable.eval(fragCoord).rgba;
        return half4(color.rg, 1.0, color.a);
    }
"""

@Language("AGSL")
const val RGB_VERTICAL = """
    uniform float2 size;
    uniform shader composable;
    
    half4 main(float2 fragCoord) {
       half4 color = composable.eval(fragCoord).rgba;
       float2 uv = fragCoord / size;
       
       if(uv.x < 0.33){
         // RED tint
         return half4(1.0, color.gba);
       }
       if(uv.x < 0.66){
         // GREEN tint
         return half4(color.r, 1.0, color.ba);
       }
       // BLUE tint
       return half4(color.rg, 1.0, color.a);
    }
"""

@Language("AGSL")
const val RGB_HORIZONTAL = """
    uniform float2 size;
    uniform shader composable;
    
    half4 main(float2 fragCoord) {
       half4 color = composable.eval(fragCoord).rgba;
       float2 uv = fragCoord / size;
       
       if(uv.y < 0.33){
         // RED tint
         return half4(1.0, color.gba);
       }
       if(uv.y < 0.66){
         // GREEN tint
         return half4(color.r, 1.0, color.ba);
       }
       // BLUE tint
       return half4(color.rg, 1.0, color.a);
    }
"""

// Source: https://www.youtube.com/watch?v=hjJesq71UXc
@Language("AGSL")
const val CHROMATIC_ABERRATION = """
    uniform float2 size;
    uniform shader composable;
    uniform float distortion;

    half4 main(float2 fragCoord) {
        half4 color = composable.eval(fragCoord).rgba;
        color.r = composable.eval(fragCoord + float2(distortion, 0)).r;
        color.b = composable.eval(fragCoord - float2(distortion, 0)).b;
        return half4(color);
    }
"""

// source: https://www.shadertoy.com/view/lsKSWR
@Language("AGSL")
const val VIGNETTE = """
    uniform float2 size;
    uniform shader composable;
    
    half4 main(float2 fragCoord) {
        half4 base = composable.eval(fragCoord);
        
        float2 uv = fragCoord / size;
        uv *= (1.0 - uv.yx);
        
        float vig = uv.x * uv.y * 15.0;
        vig = pow(vig, 0.25);
            
        return half4(base.rgb * half(vig), base.a);
    }
"""
