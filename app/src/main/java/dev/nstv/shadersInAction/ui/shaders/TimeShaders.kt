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

// inspiration: https://www.shadertoy.com/view/wtXXD2
@Language("AGSL")
const val LIQUID_WARP = """
    uniform float time;
    uniform float2 size;
    uniform shader composable;
    
    float hash1(float2 p) {
        return fract(sin(dot(p, float2(127.1, 311.7))) * 43758.5453123);
    }
    
    float noise2(float2 x) {
        float2 p = floor(x);
        float2 f = fract(x);
        f = f * f * (3.0 - 2.0 * f);
    
        float a = hash1(p + float2(0.0, 0.0));
        float b = hash1(p + float2(1.0, 0.0));
        float c = hash1(p + float2(0.0, 1.0));
        float d = hash1(p + float2(1.0, 1.0));
    
        return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
    }
    
    const mat2 mtx = mat2(0.80,  0.60,
                          -0.60, 0.80);
    
    float fbm(float2 p) {
        float f = 0.0;
    
        f += 0.500000 * noise2(p); p = (mtx * p) * 2.02;
        f += 0.250000 * noise2(p); p = (mtx * p) * 2.03;
        f += 0.125000 * noise2(p); p = (mtx * p) * 2.01;
        f += 0.062500 * noise2(p); p = (mtx * p) * 2.04;
        f += 0.031250 * noise2(p); p = (mtx * p) * 2.01;
        f += 0.015625 * noise2(p);
    
        return f / 0.96875;
    }
    
    float pattern(float2 p, float t, out float2 q, out float2 r, out float2 g) {
        q = float2(fbm(p), fbm(p + float2(10.0, 1.3)));
    
        r = float2(
            fbm(p + 4.0 * q + float2(t) + float2(1.7, 9.2)),
            fbm(p + 4.0 * q + float2(t) + float2(8.3, 2.8))
        );
    
        g = float2(
            fbm(p + 2.0 * r + float2(t * 20.0) + float2(2.0, 6.0)),
            fbm(p + 2.0 * r + float2(t * 10.0) + float2(5.0, 3.0))
        );
    
        return fbm(p + 5.5 * g + float2(-t * 7.0));
    }
    
    // ---------- main ----------
    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / size;
    
        float2 q, r, g;
        float n = pattern(fragCoord * float2(0.004), time * 0.007, q, r, g);
    
        // Domain warp amount (tweak this)
        // Bigger values = stronger distortion
        float warpStrength = 0.1;
    
        // Build a warp vector from the domain-warp intermediates.
        float2 warp = (g - 0.5) * warpStrength;
    
        // Warp in UV space, then clamp to avoid sampling outside the image
        float2 warpedUv = clamp(uv + warp, 0.0, 1.0);
    
        // Sample your image for the colors
        half4 img = composable.eval(warpedUv * size);
    
        // Optional: use the pattern to add subtle contrast (comment out if unwanted)
        float contrast = 0.85 + 0.75 * n; // ~0.85..1.60
        half3 rgb = img.rgb * half3(contrast);
    
        // Optional vignette, similar vibe to original (comment out if unwanted)
        float vig = 0.70 + 0.65 * sqrt(70.0 * uv.x * uv.y * (1.0 - uv.x) * (1.0 - uv.y));
        rgb *= half3(vig);
    
        return half4(rgb, img.a);
    }
"""

// inspiration: https://www.shadertoy.com/view/XXtBRr
@Language("AGSL")
const val BALATRO_WARP = """
    uniform float time;
    uniform float2 size;
    uniform shader composable;
    
    // ---- Configuration ----
    const float SPIN_ROTATION = -2.0;
    const float SPIN_SPEED = 7.0;
    const float2 OFFSET = float2(0.0, 0.0);
    
    const float4 COLOUR_1 = float4(0.871, 0.267, 0.231, 1.0);
    const float4 COLOUR_2 = float4(0.0, 0.42, 0.706, 1.0);
    const float4 COLOUR_3 = float4(0.086, 0.137, 0.145, 1.0);
    
    const float CONTRAST = 3.5;
    const float LIGTHING = 0.4;
    const float SPIN_AMOUNT = 0.25;
    const float PIXEL_FILTER = 745.0;
    const float SPIN_EASE = 1.0;
    
    // AGSL doesn't have preprocessor booleans; use 0.0/1.0
    // 1.0 => rotate with time, 0.0 => static angle
    const float IS_ROTATE = 0.0;
    
    // -----------------------
    
    float4 effect(float2 screenSize, float2 screenCoords) {
        float pixelSize = length(screenSize) / PIXEL_FILTER;
    
        // Pixelation + normalize to [-0.5..0.5]ish, matching original approach
        float2 uv = (floor(screenCoords * (1.0 / pixelSize)) * pixelSize - 0.5 * screenSize)
                    / length(screenSize) - OFFSET;
    
        float uvLen = length(uv);
    
        float speed = (SPIN_ROTATION * SPIN_EASE * 0.2);
        // if(IS_ROTATE) speed = time * speed;
        speed = mix(speed, time * speed, IS_ROTATE);
    
        speed += 302.2;
    
        float newPixelAngle =
            atan(uv.y, uv.x)
            + speed
            - SPIN_EASE * 20.0 * (SPIN_AMOUNT * uvLen + (1.0 - SPIN_AMOUNT));
    
        float2 mid = (screenSize / length(screenSize)) * 0.5;
    
        uv = (float2(uvLen * cos(newPixelAngle) + mid.x,
                     uvLen * sin(newPixelAngle) + mid.y) - mid);
    
        uv *= 30.0;
    
        speed = time * SPIN_SPEED;
    
        float2 uv2 = float2(uv.x + uv.y, uv.x + uv.y);
    
        // Fixed-iteration loop is fine in AGSL
        for (int i = 0; i < 5; i++) {
            uv2 += sin(max(uv.x, uv.y)) + uv;
    
            uv += 0.5 * float2(
                cos(5.1123314 + 0.353 * uv2.y + speed * 0.131121),
                sin(uv2.x - 0.113 * speed)
            );
    
            uv -= 1.0 * cos(uv.x + uv.y) - 1.0 * sin(uv.x * 0.711 - uv.y);
        }
    
        float contrastMod = (0.25 * CONTRAST + 0.5 * SPIN_AMOUNT + 1.2);
    
        float paintRes = min(2.0, max(0.0, length(uv) * 0.035 * contrastMod));
    
        float c1p = max(0.0, 1.0 - contrastMod * abs(1.0 - paintRes));
        float c2p = max(0.0, 1.0 - contrastMod * abs(paintRes));
        float c3p = 1.0 - min(1.0, c1p + c2p);
    
        float light =
            (LIGTHING - 0.2) * max(c1p * 5.0 - 4.0, 0.0) +
            LIGTHING * max(c2p * 5.0 - 4.0, 0.0);
    
        return (0.3 / CONTRAST) * COLOUR_1 +
               (1.0 - 0.3 / CONTRAST) *
               (COLOUR_1 * c1p +
                COLOUR_2 * c2p +
                float4(c3p * COLOUR_3.rgb, c3p * COLOUR_1.a)) +
               light;
    }
    
    half4 main(float2 fragCoord) {
        float2 screenSize = size;
        float2 screenCoords = fragCoord;
    
        float4 col = effect(screenSize, screenCoords);
    
        // If you want to blend over the image instead of pure procedural:
        // half4 base = composable.eval(fragCoord);
        // col = mix(float4(base.rgb, base.a), col, 0.8);
    
        return half4(col);
    }
"""

@Language("AGSL")
const val BALATRO_WARP_LIKE = """
    uniform float time;
    uniform float2 size;
    uniform shader composable;
    
    // ---- Configuration ----
    const float SPIN_ROTATION = -2.0;
    const float SPIN_SPEED = 7.0;
    const float2 OFFSET = float2(0.0, 0.0);
    
    const float CONTRAST = 3.5;
    const float SPIN_AMOUNT = 0.25;
    const float PIXEL_FILTER = 745.0;
    const float SPIN_EASE = 1.0;
    
    // 1.0 => rotates with time, 0.0 => static
    const float IS_ROTATE = 0.0;
    
    // -----------------------
    
    float2 warpUv(float2 screenSize, float2 screenCoords) {
        float pixelSize = length(screenSize) / PIXEL_FILTER;
    
        // pixelate the coordinate used for the warp (keeps that crunchy look)
        float2 uv = (floor(screenCoords * (1.0 / pixelSize)) * pixelSize - 0.5 * screenSize)
                    / length(screenSize) - OFFSET;
    
        float uvLen = length(uv);
    
        float speed = (SPIN_ROTATION * SPIN_EASE * 0.2);
        speed = mix(speed, time * speed, IS_ROTATE);
        speed += 302.2;
    
        float angle =
            atan(uv.y, uv.x)
            + speed
            - SPIN_EASE * 20.0 * (SPIN_AMOUNT * uvLen + (1.0 - SPIN_AMOUNT));
    
        float2 mid = (screenSize / length(screenSize)) * 0.5;
        uv = (float2(uvLen * cos(angle) + mid.x,
                     uvLen * sin(angle) + mid.y) - mid);
    
        uv *= 30.0;
    
        float anim = time * SPIN_SPEED;
        float2 uv2 = float2(uv.x + uv.y, uv.x + uv.y);
    
        for (int i = 0; i < 5; i++) {
            uv2 += sin(max(uv.x, uv.y)) + uv;
    
            uv += 0.5 * float2(
                cos(5.1123314 + 0.353 * uv2.y + anim * 0.131121),
                sin(uv2.x - 0.113 * anim)
            );
    
            uv -= 1.0 * cos(uv.x + uv.y) - 1.0 * sin(uv.x * 0.711 - uv.y);
        }
    
        // Use the same "paintRes" idea to produce a stable warp direction.
        float contrastMod = (0.25 * CONTRAST + 0.5 * SPIN_AMOUNT + 1.2);
        float paintRes = min(2.0, max(0.0, length(uv) * 0.035 * contrastMod));
    
        // Turn scalar fields into a 2D displacement.
        // This keeps the "look" tied to the original math but makes it usable as UV warp.
        float2 disp = float2(
            sin(uv.x + paintRes * 2.0),
            cos(uv.y - paintRes * 2.0)
        );
    
        // Warp strength: tune 0.002..0.02 depending on how strong you want it
        float warpStrength = (1.0 / PIXEL_FILTER) * 12.0;
    
        // Convert from the uv-domain to screen UV space
        float2 baseUv = screenCoords / screenSize;
    
        float2 warpedUv = baseUv + disp * warpStrength;
    
        // Clamp to avoid sampling outside image
        return clamp(warpedUv, 0.0, 1.0);
    }
    
    half4 main(float2 fragCoord) {
        float2 warpedUv = warpUv(size, fragCoord);
    
        // Sample your image: COLORS come from composable
        half4 img = composable.eval(warpedUv * size);
    
        return img;
    }
"""

// Mine, chatGPT assisted
@Language("AGSL")
const val NOISE_GRID = """
    uniform shader composable;
    uniform float2 size;
    uniform float time;

    // NOISE
    float hash21(float2 p) {
        p = fract(p * float2(123.34, 345.45));
        p += dot(p, p + 34.345);
        return fract(p.x * p.y);
    }
    
    float noise21(float2 p) {
        float2 i = floor(p);
        float2 f = fract(p);
        f = f * f * (3.0 - 2.0 * f);
    
        float a = hash21(i + float2(0.0, 0.0));
        float b = hash21(i + float2(1.0, 0.0));
        float c = hash21(i + float2(0.0, 1.0));
        float d = hash21(i + float2(1.0, 1.0));
    
        return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
    }
    
    float2 grad2(float2 p) {
        float n = sin(dot(p, float2(127.1, 311.7))) * 43758.5453;
        float a = fract(n) * 6.28318;
        return float2(cos(a), sin(a));
    }
    
    float perlin2(float2 p) {
        float2 i = floor(p);
        float2 f = fract(p);
    
        float2 u = f * f * (3.0 - 2.0 * f);
    
        float2 g00 = grad2(i + float2(0, 0));
        float2 g10 = grad2(i + float2(1, 0));
        float2 g01 = grad2(i + float2(0, 1));
        float2 g11 = grad2(i + float2(1, 1));
    
        float n00 = dot(g00, f - float2(0, 0));
        float n10 = dot(g10, f - float2(1, 0));
        float n01 = dot(g01, f - float2(0, 1));
        float n11 = dot(g11, f - float2(1, 1));
    
        float nx0 = mix(n00, n10, u.x);
        float nx1 = mix(n01, n11, u.x);
        float nxy = mix(nx0, nx1, u.y);
    
        return 0.5 + 0.5 * nxy; // normalize to 0..1
    }
    
    // "noise"
    float trigNoise21(float2 p) {
        // 0..1, stable, pseudo-random-ish
        float v = sin(dot(p, float2(12.9898, 78.233)));
        v *= cos(dot(p, float2(39.3467, 11.135)));
        return 0.5 + 0.5 * sin(v * 43758.5453);
    }
    
    // "noise"
    float trigNoiseSmooth21(float2 p) {
        float a = sin(p.x * 3.1) * cos(p.y * 4.7);
        float b = sin((p.x + p.y) * 2.3);
        float c = cos((p.x - p.y) * 2.9);
        float v = a + 0.6 * b + 0.4 * c;          // roughly -2..2
        return 0.5 + 0.5 * sin(v * 3.0);          // 0..1
    }
    
    // END NOISE
    
    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / size;
    
        // ---- square grid derived from aspect ratio ----
        float cellsAcross = 22.0;                 // tweak this (density)
        float cellSizePx = size.x / cellsAcross;  // square cell size in pixels
        float cellsDown  = size.y / cellSizePx;   // derived from size ratio
    
        float2 gridCells = float2(cellsAcross, cellsDown);
    
        float2 cell = uv * gridCells;
        float2 cellId = floor(cell);
        float2 cellUV = fract(cell);
    
        // Optional inner margin so you can "see" the squares as tiles
        float margin = 0.08;
        float edgeSoft = 0.01;
    
        float insideX =
        smoothstep(margin, margin + edgeSoft, cellUV.x) *
                (1.0 - smoothstep(1.0 - margin - edgeSoft, 1.0 - margin, cellUV.x));
    
        float insideY =
        smoothstep(margin, margin + edgeSoft, cellUV.y) *
                (1.0 - smoothstep(1.0 - margin - edgeSoft, 1.0 - margin, cellUV.y));
    
        float insideCell = insideX * insideY;
    
        // ---- blinking per-cell (noise + trig + smoothstep) ----
        float blinkSpeed = 1.0;
    
        // stable random per cell
        // Pick one noise
        float n = noise21(cellId * 0.45);
//        float n = perlin2(cellId * 0.25);
//        float n = trigNoise21(cellId);
//        float n = trigNoiseSmooth21(cellId);

    
        // 0..1 wave per cell
        float wave = 0.5 + 0.5 * sin(time * blinkSpeed + n * 6.28318);
    
        float threshold = 0.55;
        float softness  = 0.12;
        float on = smoothstep(threshold - softness, threshold + softness, wave);
    
        float mask = on * insideCell;
    
        // ---- composite ----
        half4 base = composable.eval(fragCoord);
    
        half dimAmount = 0.2; // how dark "off" cells are
        half3 outRgb = mix(base.rgb * dimAmount, base.rgb, half(mask));
    
        return half4(outRgb, base.a);
    }
"""

