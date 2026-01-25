package dev.nstv.shadersInAction.ui.shaders

import org.intellij.lang.annotations.Language

@Language("AGSL")
const val NONE = """
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

// source: ChatGPT
@Language("AGSL")
const val BLUR = """
    uniform float2 size;
    uniform shader composable;
    
    half4 main(float2 fragCoord) {
        // Derive a reasonable radius from size
        // 0.01–0.02 of the shortest edge works well for UI
        float r = min(size.x, size.y) * 0.015;
    
        // 9-tap Gaussian-ish kernel
        const float w0 = 0.2270270; // center
        const float w1 = 0.1945946; // axial
        const float w2 = 0.1216216; // diagonal
    
        half4 sum = composable.eval(fragCoord) * half(w0);
    
        float2 dx = float2(r, 0.0);
        float2 dy = float2(0.0, r);
    
        sum += composable.eval(fragCoord + dx) * half(w1);
        sum += composable.eval(fragCoord - dx) * half(w1);
        sum += composable.eval(fragCoord + dy) * half(w1);
        sum += composable.eval(fragCoord - dy) * half(w1);
    
        float2 d = float2(r, r);
        sum += composable.eval(fragCoord + d) * half(w2);
        sum += composable.eval(fragCoord + float2(r, -r)) * half(w2);
        sum += composable.eval(fragCoord + float2(-r, r)) * half(w2);
        sum += composable.eval(fragCoord - d) * half(w2);
    
        return sum;
    }
"""

// inspiration: https://medium.com/androiddevelopers/agsl-made-in-the-shade-r-7d06d14fe02a
@Language("AGSL")
const val FROSTED_GLASS = """
    uniform shader composable;
    uniform float2 size;
            
    vec4 main(vec2 fragCoord) {
        float width = size.x;        
        float height = size.y;
        
        vec4 currValue = composable.eval(fragCoord);
        float top = 0;
        if (fragCoord.y < top) {
            return currValue;
        } else {
            // Avoid blurring edges
            if (fragCoord.x > 1 && fragCoord.y > 1 &&
                    fragCoord.x < (width - 1) &&
                    fragCoord.y < (height - 1)) {
                // simple box blur - average 5x5 grid around pixel
                vec4 boxSum =
                    composable.eval(fragCoord + vec2(-2, -2)) + 
                    currValue +
                    composable.eval(fragCoord + vec2(2, 2));
                currValue = boxSum / 25;
            }
            
            const vec4 white = vec4(1);
          
            float lightenFactor = min(1.0, .6 *
                    length(fragCoord) /
                    (0.85 * length(vec2(width, 100))));
            // White in upper-left, blended increasingly
            // toward lower-right
            return mix(currValue, white, 1 - lightenFactor);
        }
    }
"""

