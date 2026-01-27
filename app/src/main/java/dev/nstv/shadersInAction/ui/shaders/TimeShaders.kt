package dev.nstv.shadersInAction.ui.shaders

import org.intellij.lang.annotations.Language

// Inspiration: https://www.shadertoy.com/view/4d2Xzw
@Language("AGSL")
const val BOKEH = """
    uniform shader composable;
    uniform float2 size;
    uniform float time;
    
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

// tweaked with chatGPT
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
    uniform shader composable;
    uniform float2 size;
    uniform float time;
    
    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / max(size, float2(1.0));
        float t = time * 0.9;
    
        float wx = sin((uv.y * 10.0) + t);
        float wy = cos((uv.x * 9.0) - t * 1.1);

        float amp = 0.0025 + 0.01 * sin((uv.x + uv.y) * 6.0 + t * 0.7);
    
        float2 offsetUv = float2(wx, wy) * amp;
        float2 offsetPx = offsetUv * size;
    
        return composable.eval(fragCoord + offsetPx);
    }
"""

// tweaked with chatGPT
@Language("AGSL")
const val FLOW_FIELD = """
    uniform shader composable;
    uniform float2 size;
    uniform float time;
    
    // --- Hash / noise ------------------------------------------------------------
    
    float hash12(float2 p) {
        // Deterministic hash in [0,1)
        float h = dot(p, float2(127.1, 311.7));
        return fract(sin(h) * 43758.5453123);
    }
    
    // Approx gaussian noise via sum-of-uniforms (Irwin–Hall-ish).
    // Returns roughly ~N(0,1) scaled to ~[-1,1] (good enough visually).
    float gauss1(float2 p) {
        float n =
            hash12(p) +
            hash12(p + 17.3) +
            hash12(p + 41.7) +
            hash12(p + 73.1);
        n = (n / 4.0) * 2.0 - 1.0; // [-1,1]
        return n;
    }
    
    float2 gauss2(float2 p) {
        return float2(gauss1(p), gauss1(p + 101.9));
    }
    
    // Smooth value noise (bilinear)
    float vnoise(float2 p) {
        float2 i = floor(p);
        float2 f = fract(p);
        float a = gauss1(i);
        float b = gauss1(i + float2(1.0, 0.0));
        float c = gauss1(i + float2(0.0, 1.0));
        float d = gauss1(i + float2(1.0, 1.0));
        float2 u = f * f * (3.0 - 2.0 * f);
        return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
    }
    
    float2 rot90(float2 v) { return float2(-v.y, v.x); }
    
    float luminance(half3 c) {
        return float(c.r) * 0.2126 + float(c.g) * 0.7152 + float(c.b) * 0.0722;
    }
    
    // --- Main -------------------------------------------------------------------
    
    half4 main(float2 fragCoord) {
        // 1) Sample the composable and compute a local gradient from its colors
        //    Use a pixel step that scales slightly with size (more stable across resolutions).
        float px = max(1.0, min(size.x, size.y) * 0.0015);
    
        half4 c  = composable.eval(fragCoord);
        half4 cxp = composable.eval(fragCoord + float2(px, 0.0));
        half4 cxm = composable.eval(fragCoord - float2(px, 0.0));
        half4 cyp = composable.eval(fragCoord + float2(0.0, px));
        half4 cym = composable.eval(fragCoord - float2(0.0, px));
    
        float l  = luminance(c.rgb);
        float gx = luminance(cxp.rgb) - luminance(cxm.rgb);
        float gy = luminance(cyp.rgb) - luminance(cym.rgb);
    
        float2 grad = float2(gx, gy);
    
        // Flow direction: perpendicular to gradient = "curl-ish" flow around color edges
        float gLen = length(grad);
        float2 curlDir = (gLen > 1e-6) ? (rot90(grad) / gLen) : float2(0.0, 0.0);
    
        // 2) Animated gaussian-ish noise field (adds fluid motion)
        //    Work in a lower-frequency domain so it looks like a flow field, not static grain.
        float2 uv = fragCoord / max(size, float2(1.0));
        float2 p = uv * 12.0 + float2(time * 0.25, -time * 0.18);
    
        // Two octave-ish noise vector
        float2 n1 = gauss2(floor(p * 24.0) + time * 3.0);
        float2 n2 = gauss2(floor(p * 48.0) - time * 5.0) * 0.5;
        float2 noiseVec = normalize(n1 + n2 + float2(1e-4, 0.0));
    
        // 3) Combine: stronger flow where the composable has structure (bigger gradient)
        //    and add some base motion so flat areas still drift.
        float structure = clamp(gLen * 6.0, 0.0, 1.0);
    
        // Strength (in pixels). Tuned to be visible but not destroy legibility.
        float baseStrength = min(size.x, size.y) * 0.004;  // overall drift
        float edgeStrength = min(size.x, size.y) * 0.010;  // extra along edges
    
        float2 flowPx =
            noiseVec * baseStrength +
            curlDir * (edgeStrength * structure);
    
        // Optional slight "breathing" to keep it alive
        float pulse = 0.75 + 0.25 * sin(time * 1.4 + l * 6.2831853);
        flowPx *= pulse;
    
        // 4) Advect: sample the composable from the flowed position
        half4 flowed = composable.eval(fragCoord + flowPx);
    
        // Keep alpha from original content (usually what you want for UI)
        return half4(flowed.rgb, c.a);
    }
"""

// Inspiration: https://www.shadertoy.com/view/lsyfDV
@Language("AGSL")
const val FLOW_FIELD_TWO = """
    uniform shader composable;
    uniform float2 size;
    uniform float time;
    
    float hash12(float2 p) {
        float h = dot(p, float2(127.1, 311.7));
        return fract(sin(h) * 43758.5453123);
    }
    
    // Smooth value noise
    float noise(float2 p) {
        float2 i = floor(p);
        float2 f = fract(p);
        float2 u = f * f * (3.0 - 2.0 * f);
    
        float a = hash12(i);
        float b = hash12(i + float2(1.0, 0.0));
        float c = hash12(i + float2(0.0, 1.0));
        float d = hash12(i + float2(1.0, 1.0));
    
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
    
    float luminance(half3 c) {
        return float(c.r) * 0.2126 + float(c.g) * 0.7152 + float(c.b) * 0.0722;
    }
    
    float2 rot90(float2 v) { return float2(-v.y, v.x); }
    
    half3 saturate3(half3 c) { return clamp(c, half3(0.0), half3(1.0)); }
    
    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / max(size, float2(1.0));
    
        // Base sample
        half4 base = composable.eval(fragCoord);
        float lum0 = luminance(base.rgb);
    
        // --- Flow direction from composable edges (gradient -> curl direction)
        float px = 1.0; // 1px finite difference (stable)
        float lumR = luminance(composable.eval(fragCoord + float2(px, 0.0)).rgb);
        float lumL = luminance(composable.eval(fragCoord - float2(px, 0.0)).rgb);
        float lumU = luminance(composable.eval(fragCoord + float2(0.0, px)).rgb);
        float lumD = luminance(composable.eval(fragCoord - float2(0.0, px)).rgb);
    
        float2 grad = float2(lumR - lumL, lumU - lumD);
        float gLen = length(grad);
        float2 curl = (gLen > 1e-6) ? (rot90(grad) / gLen) : float2(0.0, 0.0);
    
        // --- Add animated noise-based angle (Shadertoy-esque)
        float2 nP = uv * 8.0 + float2(time * 0.08, -time * 0.06);
        float a = (fbm(nP) - 0.5) * 6.2831853 * 2.0;
        float2 nDir = float2(cos(a), sin(a));
    
        // Blend: more edge-following where image has structure
        float edge = clamp(gLen * 6.0, 0.0, 1.0);
        float2 dir = normalize(mix(nDir, curl, edge) + float2(1e-4, 0.0));
    
        // --- Streamline accumulation (short advection)
        // This creates the "fibrous flow" feel without drawing explicit line segments.
        const int STEPS = 20;
    
        // Step length in pixels: scale gently with layer size
        float stepPx = min(size.x, size.y) * 0.006;
    
        float2 pos = fragCoord;
    
        half3 colAcc = half3(0.0);
        float wAcc = 0.0;
    
        for (int i = 0; i < STEPS; i++) {
            float h = float(i) / float(STEPS - 1);
            float w = 4.0 * h * (1.0 - h);          // same "weighted distribution" trick vibe
    
            // Sample color along the path
            half3 c = composable.eval(pos).rgb;
    
            // A little strand-contrast driven by luminance
            float lum = luminance(c);
            float strand = smoothstep(0.15, 0.85, lum);
    
            colAcc += c * half(w * (0.35 + 0.65 * strand));
            wAcc += w;
    
            // Recompute direction a bit along the path for more "flow"
            float2 uvi = pos / max(size, float2(1.0));
            float2 np2 = uvi * 8.0 + float2(time * 0.08, -time * 0.06);
    
            float ai = (fbm(np2) - 0.5) * 6.2831853 * 2.0;
            float2 nd = float2(cos(ai), sin(ai));
    
            // Edge-following again (cheap: reuse local gradient around pos)
            float lR = luminance(composable.eval(pos + float2(px, 0.0)).rgb);
            float lL = luminance(composable.eval(pos - float2(px, 0.0)).rgb);
            float lU = luminance(composable.eval(pos + float2(0.0, px)).rgb);
            float lD = luminance(composable.eval(pos - float2(0.0, px)).rgb);
    
            float2 g = float2(lR - lL, lU - lD);
            float gl = length(g);
            float2 cu = (gl > 1e-6) ? (rot90(g) / gl) : float2(0.0, 0.0);
            float ed = clamp(gl * 6.0, 0.0, 1.0);
    
            dir = normalize(mix(nd, cu, ed) + float2(1e-4, 0.0));
    
            pos += dir * stepPx;
        }
    
        half3 flowed = colAcc / half(max(wAcc, 1e-6));
    
        // Gentle toning similar to the Shadertoy post feel
        // Slight vibrance + mild lift in midtones
        half l = half(lum0);
        flowed = mix(half3(l), flowed, half(1.10));
        flowed = sqrt(max(flowed, half3(0.0)));
    
        // Keep original alpha
        return half4(saturate3(flowed), base.a);
    }

"""

// inspiration: https://www.shadertoy.com/view/mtyGWy
@Language("AGSL")
const val FRACTAL="""
    uniform shader composable;
    uniform float time;
    uniform float2 size;

    float sdBox(float2 p, float2 b) {
        float2 d = abs(p) - b;
        return length(max(d, 0.0)) + min(max(d.x, d.y), 0.0);
    }
    
    half4 main(float2 fragCoord) {
        half4 base = composable.eval(fragCoord);
        float2 uv  = (fragCoord * 2.0 - size.xy) / size.y;
        float2 uv0 = uv;
    
        half3 finalColor = half3(0.0);
    
        for (int layer = 0; layer < 10; layer++) {
            float i = float(layer);
    
            uv = fract(uv * 2.0) - 0.5;
    
            // Circle version
            float d = length(uv) * exp(-length(uv0));
    
            // Square version
            // float2 square = float2(0.9, 0.6);
            // float d = sdBox(uv, square);
    
            half3 color = base.rgb;
    
            d = sin(d * 8.0 + time) / 8.0;
            d = abs(d);
    
            // avoid division-by-zero explosions
            d = max(d, 1e-5);
    
            d = pow(0.001 / d, 1.2);
    
            finalColor += color * half(d);
        }
        return half4(finalColor, base.a);
    }
"""
