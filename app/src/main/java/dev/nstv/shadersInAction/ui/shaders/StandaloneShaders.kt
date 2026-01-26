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
const val SOLO_RB_VERTICAL = """
    uniform float time;
    uniform float2 size;
    
    half4 main(float2 fragCoord) {
       float2 uv = fragCoord / size;
       
       if(uv.x <= 0.5){
            // RED tint
            return half4(1.0, 0.0, 0.0, 1.0);
       }
       
        // BLUE tint
        return half4(0.0, 0.0, 1.0, 1.0);
    }
"""

@Language("AGSL")
const val SOLO_RB_VERTICAL_MIX = """
    uniform float time;
    uniform float2 size;
    
    half4 main(float2 fragCoord) {
       float2 uv = fragCoord / size;
       
       half4 red = half4(1.0, 0.0, 0.0, 1.0);
       half4 blue = half4(0.0, 0.0, 1.0, 1.0);
       
       return mix(red, blue, uv.x);
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

// inspiration: https://www.shadertoy.com/view/mtyGWy
@Language("AGSL")
const val SOLO_FRACTAL="""
    uniform float time;
    uniform float2 size;
    
    half3 palette(float t) {
        half3 a = half3(0.5, 0.5, 0.5);
        half3 b = half3(0.5, 0.5, 0.5);
        half3 c = half3(1.0, 1.0, 1.0);
        half3 d = half3(0.50, 0.20, 0.25);
        return a + b * cos(6.28318 * (c * half(t) + d));
    }
    
    float sdBox(float2 p, float2 b) {
        float2 d = abs(p) - b;
        return length(max(d, 0.0)) + min(max(d.x, d.y), 0.0);
    }
    
    half4 main(float2 fragCoord) {
        float2 uv  = (fragCoord * 2.0 - size.xy) / size.y;
        float2 uv0 = uv;
    
        half3 finalColor = half3(0.0);
    
        for (int layer = 0; layer < 4; layer++) {
            float i = float(layer);
    
            uv = fract(uv * 2.0) - 0.5;
    
            // Circle version
            float d = length(uv) * exp(-length(uv0));
    
            // Square version
            // float2 square = float2(0.9, 0.6);
            // float d = sdBox(uv, square);
    
            half3 color = palette(length(uv0) + i + time * 0.4);
    
            d = sin(d * 8.0 + time) / 8.0;
            d = abs(d);
    
            // avoid division-by-zero explosions
            d = max(d, 1e-5);
    
            d = pow(0.001 / d, 1.2);
    
            finalColor += color * half(d);
        }
    
        return half4(finalColor, 1.0);
    }
"""
