package dev.nstv.shadersInAction.ui.shaders

import org.intellij.lang.annotations.Language

// Inspiration: https://www.shadertoy.com/view/4d2Xzw
@Language("AGSL")
const val BOKEH = """
    uniform float time;
    uniform float2 size;
    uniform shader composable;
    
    const float GOLDEN_ANGLE = 2.3999632;
    const int ITERATIONS = 150;

    float2 rotateGolden(float2 v) {
        float c = cos(GOLDEN_ANGLE);
        float s = sin(GOLDEN_ANGLE);
        return float2(c * v.x + s * v.y, -s * v.x + c * v.y);
    }
    
    half4 main(float2 fragCoord) {
        float t = mod(time * 0.2 + 0.25, 3.0);
        float radius = 0.8 - 0.8 * cos(t * 6.2831853);
    
        half3 acc = half3(0.0);
        half3 div = half3(0.0);
    
        float r = 1.0;
    
        // Do offsets in pixels. Use min(size.x, size.y) so radius feels consistent across aspect ratios.
        float pxScale = min(size.x, size.y);
        float2 vangle = float2(0.0, radius * 0.01 * pxScale / sqrt(float(ITERATIONS)));
    
        for (int j = 0; j < ITERATIONS; j++) {
            r += 1.0 / r;
            vangle = rotateGolden(vangle);
    
            float2 samplePos = fragCoord + (r - 1.0) * vangle;
    
            half4 h = composable.eval(samplePos);
    
            half3 col = h.rgb;
            col = col * col * half(1.8);
    
            half3 bokeh = half3(pow(col.r, 4.0), pow(col.g, 4.0), pow(col.b, 4.0));
            acc += col * bokeh;
            div += bokeh;
        }
    
        half3 rgb = acc / max(div, half3(1e-6));
    
        // Preserve original alpha at this pixel
        half a = composable.eval(fragCoord).a;
        return half4(rgb, a);
    }
"""

// source: ChatGPT
@Language("AGSL")
const val LIQUID = """
    uniform shader composable;
    uniform float2 size;
    uniform float time; 
    
    float hash12(float2 p) {
        float h = dot(p, float2(127.1, 311.7));
        return fract(sin(h) * 43758.5453123);
    }
    
    float2 noise2(float2 p) {
        // cheap pseudo-noise vector in [-1,1]
        float n1 = hash12(p);
        float n2 = hash12(p + 19.19);
        return float2(n1, n2) * 2.0 - 1.0;
    }
    
    half3 saturate3(half3 c) { return clamp(c, half3(0.0), half3(1.0)); }
    
    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / max(size, float2(1.0));
    
        // Centered coords for lens-like falloff
        float2 p = uv * 2.0 - 1.0;
        p.x *= size.x / size.y; // aspect-correct
    
        float dist = length(p);
    
        // ---- 1) Refraction field (aware of edges)
        // Stronger near edges, softer in the center.
        float edge = smoothstep(0.15, 0.95, dist);
    
        // Wavy distortion (animated) - feels "liquid"
        float t = time * 0.6;
        float2 wave =
            float2(
                sin((uv.y + t) * 10.0) + sin((uv.y - t * 0.8) * 23.0),
                sin((uv.x - t) * 12.0) + sin((uv.x + t * 0.7) * 19.0)
            );
    
        // Add tiny noisy micro-ripple so it doesn't look like simple sine warps
        float2 micro = noise2(floor(fragCoord * 0.5) + t * 10.0);
    
        float2 refractVec = (wave * 0.0025 + micro * 0.0012) * (0.35 + 1.25 * edge);
    
        // Convert normalized offset to pixels
        float2 refractPx = refractVec * size;
    
        // ---- 2) Chromatic aberration (tiny RGB offsets, very "lens")
        float ca = (0.6 + 1.2 * edge) * 1.25; // strength
        float2 caOff = refractPx * (0.25 * ca);
    
        half4 cr = composable.eval(fragCoord + refractPx + caOff);
        half4 cg = composable.eval(fragCoord + refractPx);
        half4 cb = composable.eval(fragCoord + refractPx - caOff);
    
        half3 rgb = half3(cr.r, cg.g, cb.b);
    
        // ---- 3) Specular sheen / highlight band (the "glass" shine)
        // A moving diagonal highlight that intensifies near edges.
        float diag = uv.x * 0.75 + uv.y * 0.25;
        float band = exp(-pow((diag - 0.55) * 5.0, 2.0));
        float shimmer = 0.5 + 0.5 * sin((uv.x * 6.0 + uv.y * 3.0) + time * 1.6);
        float spec = band * (0.35 + 0.65 * shimmer) * (0.25 + 1.2 * edge);
    
        rgb += half3(half(spec) * 0.20);
    
        // ---- 4) Edge brightening (lens rim)
        float rim = smoothstep(0.55, 0.98, dist);
        rgb += half3(half(rim) * 0.12);
    
        // ---- 5) Slight blur-ish softening (very subtle, keeps it "liquid" not "frosted")
        // One extra centered sample blend (cheap)
        half3 base = composable.eval(fragCoord).rgb;
        rgb = mix(base, rgb, half(0.78));
    
        // ---- 6) Optional tint (very light)
        rgb *= half3(0.98, 0.995, 1.02);
    
        // Keep alpha from original
        half a = composable.eval(fragCoord).a;
    
        return half4(saturate3(rgb), a);
    }
"""

@Language("AGSL")
const val RIPPLE = """
    uniform float time;
    uniform float2 size;
    uniform shader composable;
    
    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / max(size, float2(1.0));
        float t = time * 0.8;
    
        float wx = sin((uv.y * 10.0) + t);
        float wy = cos((uv.x * 9.0) - t * 1.1);

        float amp = 0.0025 + 0.0015 * sin((uv.x + uv.y) * 6.0 + t * 0.7);
    
        float2 offsetUv = float2(wx, wy) * amp;
        float2 offsetPx = offsetUv * size;
    
        return composable.eval(fragCoord + offsetPx);
    }
"""

// source: ChatGPT
@Language("AGSL")
const val RIPPLE_POINTER_SIMPLE = """
    uniform shader composable;
    uniform float2 size;
    uniform float time;
    uniform float2 pointer; 
    
    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / max(size, float2(1.0));
        float2 puv = pointer / max(size, float2(1.0));
    
        // Distance to pointer (aspect-correct so falloff stays circular)
        float2 d = uv - puv;
        d.x *= size.x / size.y;
        float dist = length(d);
    
        // Influence radius (normalized UV space)
        float influence = smoothstep(0.35, 0.0, dist);
    
        float t = time * 0.9;
    
        // Radial ripple centered on the pointer
        float waves = sin(dist * 60.0 - t * 6.0);
    
        // Subtle wobble to avoid perfect rings
        float wobble = sin((uv.x * 8.0 + uv.y * 6.0) + t) * 0.5;
    
        // Amplitude grows near the pointer
        float amp = (0.002 + 0.006 * influence) * (0.6 + 0.4 * waves) * (0.85 + 0.15 * wobble);
    
        // Offset direction: outward from the pointer
        float2 dir = (dist > 1e-4) ? (d / dist) : float2(0.0, 0.0);
    
        // Convert UV offset to pixel offset for composable sampling
        float2 offsetPx = (dir * amp) * size;
    
        return composable.eval(fragCoord + offsetPx);
    }
"""
