package dev.nstv.shadersInAction.ui.shaders

import org.intellij.lang.annotations.Language


// tweaked with chatGPT
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

// tweaked with chatGPT
@Language("AGSL")
const val WATER_COLOR = """
    uniform shader composable;
    uniform float2 size;
    
    float hash12(float2 p) {
        float h = dot(p, float2(127.1, 311.7));
        return fract(sin(h) * 43758.5453123);
    }
    
    float noise(float2 p) {
        float2 i = floor(p);
        float2 f = fract(p);
        float a = hash12(i);
        float b = hash12(i + float2(1.0, 0.0));
        float c = hash12(i + float2(0.0, 1.0));
        float d = hash12(i + float2(1.0, 1.0));
        float2 u = f * f * (3.0 - 2.0 * f);
        return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
    }
    
    float fbm(float2 p) {
        float v = 0.0;
        float a = 0.5;
        for (int i = 0; i < 4; i++) {
            v += a * noise(p);
            p = p * 2.0 + 17.0;
            a *= 0.5;
        }
        return v;
    }
    
    half3 saturate3(half3 c) { return clamp(c, half3(0.0), half3(1.0)); }
    
    float luminance(half3 c) {
        return float(c.r) * 0.2126 + float(c.g) * 0.7152 + float(c.b) * 0.0722;
    }
    
    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / max(size, float2(1.0));
    
        half4 base = composable.eval(fragCoord);
    
        // --- Edge direction (luminance gradient)
        float px = max(1.0, min(size.x, size.y) * 0.0012);
    
        float lxp = luminance(composable.eval(fragCoord + float2(px, 0.0)).rgb);
        float lxm = luminance(composable.eval(fragCoord - float2(px, 0.0)).rgb);
        float lyp = luminance(composable.eval(fragCoord + float2(0.0, px)).rgb);
        float lym = luminance(composable.eval(fragCoord - float2(0.0, px)).rgb);
    
        float2 grad = float2(lxp - lxm, lyp - lym);
        float gLen = length(grad);
        float2 dir = (gLen > 1e-6) ? (grad / gLen) : float2(0.0, 0.0);
        float2 perp = float2(-dir.y, dir.x);
    
        // --- Paper grain
        float paper = fbm(uv * 220.0);
        paper = (paper - 0.5) * 0.12;
    
        // --- Edge strength
        float edge = clamp(gLen * 6.0, 0.0, 1.0);
    
        // --- Water bleed (few taps, edge-aware)
        float bleed = min(size.x, size.y) * (0.002 + 0.010 * edge); // pixels
        float2 bpx = perp * bleed;          // across-edge bleed
        float2 apx = dir * (bleed * 0.35);  // slight along-edge wash
    
        half4 s0 = base;
        half4 s1 = composable.eval(fragCoord + bpx);
        half4 s2 = composable.eval(fragCoord - bpx);
        half4 s3 = composable.eval(fragCoord + apx);
        half4 s4 = composable.eval(fragCoord - apx);
    
        half4 wash = s0 * half(0.44)
                  + (s1 + s2) * half(0.18)
                  + (s3 + s4) * half(0.10);
    
        // --- Pigment pooling + saturation lift near edges
        half3 rgb = wash.rgb;
        half lum = half(luminance(rgb));
        half3 satUp = mix(half3(lum), rgb, half(1.18));
        rgb = mix(rgb, satUp, half(edge * 0.6));
        rgb *= half(1.0 - edge * 0.10);
    
        // --- Wash steps (mild posterization)
        half steps = half(24.0);
        rgb = floor(rgb * steps + half(0.5)) / steps;
    
        // --- Apply paper grain + slight warm tint
        rgb += half3(half(paper));
        rgb *= half3(1.02, 1.01, 0.99);
    
        return half4(saturate3(rgb), base.a);
    }

"""

@Language("AGSL")
const val OIL_PAINTING = """
    uniform shader composable;
    uniform float2 size;
    
    float luminance(half3 c) {
        return float(c.r) * 0.2126 + float(c.g) * 0.7152 + float(c.b) * 0.0722;
    }
    
    half3 saturate3(half3 c) { return clamp(c, half3(0.0), half3(1.0)); }
    
    half3 sampleClamped(float2 p) {
        // Clamp to valid pixel area to avoid sampling transparent black outside bounds
        float2 q = clamp(p, float2(0.5), size - float2(0.5));
        return composable.eval(q).rgb;
    }
    
    half4 main(float2 fragCoord) {
        half4 base = composable.eval(clamp(fragCoord, float2(0.5), size - float2(0.5)));
        float l0 = luminance(base.rgb);
    
        // Radius derived from size, clamped (AGSL-friendly)
        float rF = max(2.0, min(size.x, size.y) * 0.004);
        int R = int(clamp(rF, 2.0, 6.0)); // 2..6 px neighborhood
    
        float bestScore = 1e9;
        half3 bestColor = base.rgb;
    
        // Fixed loop bounds (AGSL-safe), but only consider within R
        for (int y = -6; y <= 6; y++) {
            for (int x = -6; x <= 6; x++) {
                // AGSL-safe bounds check 
                if (x > R || x < -R || y > R || y < -R) continue;
    
                float2 p = fragCoord + float2(float(x), float(y));
                half3 c = sampleClamped(p);
    
                float l = luminance(c);
    
                // Prefer similar luminance (forms blobs) and slightly prefer closer pixels (stability)
                float dl = abs(l - l0);
                float dist2 = float(x * x + y * y);
                float score = dl * 1.25 + dist2 * 0.01;
    
                if (score < bestScore) {
                    bestScore = score;
                    bestColor = c;
                }
            }
        }
    
        // Chunkier “paint” feel: mild quantization
        half steps = half(18.0);
        half3 rgb = floor(bestColor * steps + half(0.5)) / steps;
    
        // Mild pooling: darken slightly where the selection deviates more
        half edge = half(clamp(bestScore * 2.0, 0.0, 1.0));
        rgb *= half3(1.0 - 0.10 * edge);
    
        return half4(saturate3(rgb), base.a);
    }

"""

//source: https://github.com/skydoves/Cloudy/blob/main/cloudy/src/commonMain/kotlin/com/skydoves/cloudy/LiquidGlassShaderSource.kt
@Language("AGSL")
const val LIQUID_GLASS = """
    uniform float2 resolution;
    uniform float2 lensCenter;
    uniform float2 lensSize;
    uniform float cornerRadius;
    uniform float refraction;
    uniform float curve;
    uniform float dispersion;
    uniform float saturation;
    uniform float contrast;
    uniform float4 tint;
    uniform float edge;
    uniform shader content;
    
    const float SMOOTH_EDGE_PX = 1.5;
    
    // Signed distance to a box with rounded corners
    // Negative = inside, Positive = outside, Zero = on boundary
    float boxRoundedSDF(float2 p, float2 halfDim, float r) {
        float2 d = abs(p) - halfDim + float2(r);
        float exterior = length(max(d, 0.0));
        float interior = min(max(d.x, d.y), 0.0);
        return exterior + interior - r;
    }
    
    // Outward-facing direction vector from the lens surface
    float2 lensNormalDirection(float2 p, float2 halfDim, float r) {
        float2 d = abs(p) - halfDim + float2(r);
        float2 s = float2(p.x >= 0.0 ? 1.0 : -1.0, p.y >= 0.0 ? 1.0 : -1.0);
    
        if (max(d.x, d.y) > 0.0) {
            return s * normalize(max(d, 0.0));
        }
        return d.x > d.y ? float2(s.x, 0.0) : float2(0.0, s.y);
    }
    
    // Perceptual brightness (ITU-R BT.709 standard)
    float toBrightness(half3 c) {
        return dot(c, half3(0.2126, 0.7152, 0.0722));
    }
    
    // Color processing: vibrancy, intensity adjustment, and color overlay
    half3 processColor(half3 src, float vibrancy, float intensity, float4 overlay) {
        float mono = toBrightness(src);
        half3 vibrant = half3(clamp(mix(half3(mono), src, vibrancy), 0.0, 1.0));
        half3 adjusted = half3(clamp((vibrant - 0.5) * intensity + 0.5, 0.0, 1.0));
        return mix(adjusted, half3(overlay.rgb), overlay.a);
    }
    
    half4 main(float2 xy) {
        float2 halfDim = lensSize * 0.5;
        float r = min(cornerRadius, min(halfDim.x, halfDim.y));
    
        float2 p = xy - lensCenter;
        float sdf = boxRoundedSDF(p, halfDim, r);
    
        // Skip pixels outside the lens boundary
        if (sdf > SMOOTH_EDGE_PX) {
            return content.eval(xy);
        }
    
        float2 normal = lensNormalDirection(p, halfDim, r);
    
        // Compute refracted sample position
        float2 sampleXY = xy;
        if (refraction > 0.0 && curve > 0.0) {
            float minDim = min(halfDim.x, halfDim.y);
            float depth = clamp(-sdf / (minDim * refraction), 0.0, 1.0);
            float curvature = 1.0 - depth;
            float bend = 1.0 - sqrt(1.0 - curvature * curvature);
            sampleXY = xy - bend * curve * minDim * normal;
        }
    
        // RGB channel separation for prismatic effect
        half4 pixel;
        if (dispersion > 0.0) {
            float2 normP = p / halfDim;
            float2 shift = dispersion * normP * normP * normP * min(halfDim.x, halfDim.y) * 0.1;
    
            float2 xyR = sampleXY - shift;
            float2 xyG = sampleXY;
            float2 xyB = sampleXY + shift;
    
            float sdfR = boxRoundedSDF(xyR - lensCenter, halfDim, r);
            float sdfB = boxRoundedSDF(xyB - lensCenter, halfDim, r);
    
            half4 gVal = content.eval(xyG);
            half4 rVal = (sdfR <= 0.0) ? content.eval(xyR) : gVal;
            half4 bVal = (sdfB <= 0.0) ? content.eval(xyB) : gVal;
    
            pixel = half4(rVal.r, gVal.g, bVal.b, gVal.a);
        } else {
            pixel = content.eval(sampleXY);
        }
    
        // Handle fully transparent samples
        if (pixel.a <= 0.0) {
            pixel = content.eval(xy);
        }
    
        pixel.rgb = processColor(pixel.rgb, saturation, contrast, tint);
    
        // Specular rim highlight
        if (edge > 0.0) {
            float rimBlend = smoothstep(-edge * 10.0, 0.0, sdf);
            float2 lightVec = normalize(float2(-1.0, -1.0));
            float specular = abs(dot(normal, lightVec));
            pixel.rgb += half3(rimBlend * specular * edge);
        }
    
        // Anti-aliased edge transition
        float alpha = 1.0 - smoothstep(-SMOOTH_EDGE_PX * 0.5, SMOOTH_EDGE_PX * 0.5, sdf);
        half4 bg = content.eval(xy);
        return mix(bg, pixel, alpha);
    }
"""
