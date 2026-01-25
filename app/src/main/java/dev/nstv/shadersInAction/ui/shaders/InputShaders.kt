package dev.nstv.shadersInAction.ui.shaders

import org.intellij.lang.annotations.Language

// tweaked with chatGPT
@Language("AGSL")
const val RIPPLE_POINTER_SIMPLE = """
    uniform shader composable;
    uniform float2 size;
    uniform float time;
    uniform float2 pointer; 
    uniform float2 pointerDelta;
    
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

// Inspiration: https://www.shadertoy.com/view/4dcGW2
@Language("AGSL")
const val EXPANSIVE_DIFFUSION = """
    uniform shader composable;
    uniform float2 size;
    uniform float  time;
    uniform float2 pointer;
    uniform float2 pointerDelta;
    
    float hash21(float2 p) {
        p = fract(p * float2(123.34, 456.21));
        p += dot(p, p + 34.345);
        return fract(p.x * p.y);
    }
    
    float noise2(float2 p) {
        float2 i = floor(p);
        float2 f = fract(p);
    
        float a = hash21(i);
        float b = hash21(i + float2(1.0, 0.0));
        float c = hash21(i + float2(0.0, 1.0));
        float d = hash21(i + float2(1.0, 1.0));
    
        float2 u = f * f * (3.0 - 2.0 * f);
        return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
    }
    
    float luminance(float3 c) {
        return dot(c, float3(0.2126, 0.7152, 0.0722));
    }
    
    float2 cmul(float2 a, float2 b) {
        return float2(a.x*b.x - a.y*b.y, a.x*b.y + a.y*b.x);
    }
    
    float sigmoid(float x) {
        return 2.0 / (1.0 + exp2(-x)) - 1.0;
    }
    
    float conetip(float2 uv, float2 pos, float s, float minV) {
        float2 aspect = float2(1.0, size.y / size.x);
        return max(minV, 1.0 - length((uv - pos) * aspect / s));
    }
    
    float warpFilter(float2 uv, float2 pos, float s, float ramp) {
        return 0.5 + sigmoid(conetip(uv, pos, s, -16.0) * ramp) * 0.5;
    }
    
    float2 vortexWarp(float2 uv, float2 pos, float s, float ramp, float2 rot) {
        float2 aspect = float2(1.0, size.y / size.x);
        float2 rotUv = pos + cmul((uv - pos) * aspect, rot) / aspect;
        float f = warpFilter(uv, pos, s, ramp);
        return mix(uv, rotUv, f);
    }
    
    float2 vortexPairWarp(float2 uv, float2 pos, float2 vel) {
        float2 aspect = float2(1.0, size.y / size.x);
        float ramp = 5.0;
        float d = 0.20;
    
        float l = length(vel);
        float2 p1 = pos;
        float2 p2 = pos;
    
        if (l > 1e-5) {
            float2 normal = normalize(float2(-vel.y, vel.x)) / aspect;
            p1 = pos - normal * d * 0.5;
            p2 = pos + normal * d * 0.5;
        }
    
        float w = (l / d) * 2.0;
        float2 r1 = float2(cos(w),  sin(w));
        float2 r2 = float2(cos(-w), sin(-w));
    
        float2 a = vortexWarp(uv, p1, d, ramp, r1);
        float2 b = vortexWarp(uv, p2, d, ramp, r2);
        return (a + b) * 0.5;
    }
    
    float4 sampleComposable(float2 uv) {
        // composable expects pixel coords
        return composable.eval(uv * size);
    }
    
    float3 blur9(float2 uv, float2 px) {
        float w0 = 0.16;
        float w1 = 0.15;
        float w2 = 0.12;
    
        float3 sum = sampleComposable(uv).rgb * w0;
    
        sum += sampleComposable(uv + float2( 1.0, 0.0) * px).rgb * w1;
        sum += sampleComposable(uv + float2(-1.0, 0.0) * px).rgb * w1;
        sum += sampleComposable(uv + float2( 0.0, 1.0) * px).rgb * w1;
        sum += sampleComposable(uv + float2( 0.0,-1.0) * px).rgb * w1;
    
        sum += sampleComposable(uv + float2( 2.0, 0.0) * px).rgb * w2;
        sum += sampleComposable(uv + float2(-2.0, 0.0) * px).rgb * w2;
        sum += sampleComposable(uv + float2( 0.0, 2.0) * px).rgb * w2;
        sum += sampleComposable(uv + float2( 0.0,-2.0) * px).rgb * w2;
    
        // normalize-ish: 0.16 + 4*0.15 + 4*0.12 = 1.24
        return sum / 1.24;
    }
    
    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / size;
        float2 px = 1.0 / size;
    
        float2 pointerUv = pointer / size;
    
        // vortex warp (pointerDelta expected in UV space)
        float2 vel = pointerDelta * float2(1.0, size.y / size.x) * 1.4;
        uv = vortexPairWarp(uv, pointerUv, vel);
    
        float3 base = sampleComposable(uv).rgb;
        float3 b = blur9(uv, px * 2.0);
    
        // gradients from blurred luminance
        float2 d = px * 2.0;
        float lbx1 = luminance(blur9(uv + float2(d.x, 0.0), px * 2.0));
        float lbx0 = luminance(blur9(uv - float2(d.x, 0.0), px * 2.0));
        float lby1 = luminance(blur9(uv + float2(0.0, d.y), px * 2.0));
        float lby0 = luminance(blur9(uv - float2(0.0, d.y), px * 2.0));
        float2 grad = float2(lbx1 - lbx0, lby1 - lby0);
    
        float l0 = luminance(base);
        float lB = luminance(b);
    
        float n = noise2(uv * 180.0 + float2(42.0, 56.0) + time * 0.35);
    
        float field = l0;
        field += (n - 0.5) * 0.020;
        field -= 0.010;
        field -= (lB - l0) * 0.55;
        field += dot(grad, grad) * 0.75;
    
        field = clamp(field, 0.0, 1.0);
    
        // relight
        float2 aspect = float2(1.0, size.y / size.x);
        float2 lightSize = float2(4.0, 4.0);
    
        float2 displacement = grad * lightSize * 2.0;
        float2 p = 0.5 + (uv - 0.5) * aspect * lightSize + displacement;
        float2 m = 0.5 + (pointerUv - 0.5) * aspect * lightSize;
    
        float light = pow(max(1.0 - distance(p, m), 0.0), 4.0);
    
        float3 rd = field.xxx * float3(0.7, 1.5, 2.0) - float3(0.3, 1.0, 1.0);
    
        float inv = 1.0 - field;
        float3 lit = mix(rd, float3(8.0, 6.0, 2.0), light * 0.75 * inv);
    
        // mild tonemap
        lit = lit / (1.0 + max(lit.r, max(lit.g, lit.b)) * 0.35);
    
        return half4(half3(lit), 1.0);
    }
"""
