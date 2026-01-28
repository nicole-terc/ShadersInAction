package dev.nstv.shadersInAction.ui.shaders

import android.graphics.RuntimeShader

object Shaders {

    // Standalone shaders
    val solo_white = RuntimeShader(SOLO_WHITE)
    val solo_red = RuntimeShader(SOLO_RED)
    val solo_green = RuntimeShader(SOLO_GREEN)
    val solo_blue = RuntimeShader(SOLO_BLUE)
    val solo_rb_vertical = RuntimeShader(SOLO_RB_VERTICAL)
    val solo_rb_vertical_mix = RuntimeShader(SOLO_RB_VERTICAL_MIX)
    val solo_rgb_vertical = RuntimeShader(SOLO_RGB_VERTICAL)
    val solo_rgb_horizontal = RuntimeShader(SOLO_RGB_HORIZONTAL)
    val solo_fractal = RuntimeShader(SOLO_FRACTAL)

    // Simple Shaders
    val none = RuntimeShader(NONE).apply {
        setFloatUniform("time", 0f)
    }
    val red = RuntimeShader(RED)
    val green = RuntimeShader(GREEN)
    val blue = RuntimeShader(BLUE)
    val rbVertical = RuntimeShader(RB_VERTICAL)
    val rgbVertical = RuntimeShader(RGB_VERTICAL)
    val rgbHorizontal = RuntimeShader(RGB_HORIZONTAL)
    val grayscale = RuntimeShader(GRAYSCALE)
    val chromaticAberration = RuntimeShader(CHROMATIC_ABERRATION).apply {
        setFloatUniform("distortion", 30f)
    }
    val vignette = RuntimeShader(VIGNETTE)
    val blur = RuntimeShader(BLUR)
    val frostedGlass = RuntimeShader(FROSTED_GLASS)

    // With time parameter
    val bokeh = RuntimeShader(BOKEH).apply {
        setFloatUniform("time", 0f)
    }
    val liquid = RuntimeShader(LIQUID).apply {
        setFloatUniform("time", 0f)
    }
    val waterColor = RuntimeShader(WATER_COLOR)
    val oilPainting = RuntimeShader(OIL_PAINTING)

    val ripple = RuntimeShader(RIPPLE).apply {
        setFloatUniform("time", 0f)
    }
    val flowField = RuntimeShader(FLOW_FIELD).apply {
        setFloatUniform("time", 100f)
    }
    val flowField2 = RuntimeShader(FLOW_FIELD_TWO).apply {
        setFloatUniform("time", 100f)
    }
    val fractal = RuntimeShader(FRACTAL).apply {
        setFloatUniform("time", 0f)
    }
    val liquidWarp = RuntimeShader(LIQUID_WARP)
    val balatroWarp = RuntimeShader(BALATRO_WARP)
    val balatroWarpLike = RuntimeShader(BALATRO_WARP_LIKE)
    val noiseGrid = RuntimeShader(NOISE_GRID)

    // with pointer and time
    val ripplePointer = RuntimeShader(RIPPLE_POINTER_SIMPLE)
    val expansiveDiffusion = RuntimeShader(EXPANSIVE_DIFFUSION)
    val expansiveDiffusionTwo = RuntimeShader(EXPANSIVE_DIFFUSION_TWO)

    fun getStandaloneShaders() = mapOf(
        "solo white" to solo_white,
        "solo red" to solo_red,
        "solo green" to solo_green,
        "solo blue" to solo_blue,
        "solo rb vertical" to solo_rb_vertical,
        "solo rb vertical mix" to solo_rb_vertical_mix,
        "solo rgb vertical" to solo_rgb_vertical,
        "solo rgb horizontal" to solo_rgb_horizontal,
        "solo fractal" to solo_fractal,
    )


    fun getRenderShaders(
    ) = mapOf(
        "none" to none,
        "red" to red,
        "green" to green,
        "blue" to blue,
        "rb vertical" to rbVertical,
        "rgb vertical" to rgbVertical,
        "rgb horizontal" to rgbHorizontal,
        "grayscale" to grayscale,
        "chromatic aberration" to chromaticAberration,
        "vignette" to vignette,
        "bokeh" to bokeh,
        "blur" to blur,
        "frosted glass" to frostedGlass,
        "liquid" to liquid,
        "ripple" to ripple,
        "flow field" to flowField,
        "flow field 2" to flowField2,
        "water color" to waterColor,
        "oil painting" to oilPainting,
        "fractal" to fractal,
    )

    fun getShadersWithTime() = mapOf(
        "none" to none,
        "ripple" to ripple,
        "liquid" to liquid,
        "bokeh" to bokeh,
        "flow field" to flowField,
        "flow field 2" to flowField2,
        "fractal" to fractal,
        "liquid warp" to liquidWarp,
//        "balatro warp" to balatroWarp,
        "balatro warp like" to balatroWarpLike,
        "noise grid" to noiseGrid,
    )

    fun getShadersWithPointer() = mapOf(
        "expansive diffusion" to expansiveDiffusion,
//        "expansive diffusion 2" to expansiveDiffusionTwo,
        "ripple pointer" to ripplePointer,
    )
}
