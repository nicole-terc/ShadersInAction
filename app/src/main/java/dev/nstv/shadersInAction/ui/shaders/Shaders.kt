package dev.nstv.shadersInAction.ui.shaders

import android.graphics.RuntimeShader

object Shaders {

    // Standalone shaders
    val solo_white = RuntimeShader(SOLO_WHITE)
    val solo_red = RuntimeShader(SOLO_RED)
    val solo_green = RuntimeShader(SOLO_GREEN)
    val solo_blue = RuntimeShader(SOLO_BLUE)
    val solo_rgb_vertical = RuntimeShader(SOLO_RGB_VERTICAL)
    val solo_rgb_horizontal = RuntimeShader(SOLO_RGB_HORIZONTAL)

    // Simple Shaders
    val none = RuntimeShader(NONE).apply {
        setFloatUniform("time", 0f)
    }
    val red = RuntimeShader(RED)
    val green = RuntimeShader(GREEN)
    val blue = RuntimeShader(BLUE)
    val rgbVertical = RuntimeShader(RGB_VERTICAL)
    val rgbHorizontal = RuntimeShader(RGB_HORIZONTAL)
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

    // with pointer and time
    val ripplePointer = RuntimeShader(RIPPLE_POINTER_SIMPLE)
    val expansiveDiffusion = RuntimeShader(EXPANSIVE_DIFFUSION)
    val expansiveDiffusionTwo = RuntimeShader(EXPANSIVE_DIFFUSION_TWO)

    fun getStandaloneShaders() = mapOf(
        "solo white" to solo_white,
        "solo red" to solo_red,
        "solo green" to solo_green,
        "solo blue" to solo_blue,
        "solo rgb vertical" to solo_rgb_vertical,
        "solo rgb horizontal" to solo_rgb_horizontal,
    )

    fun getRenderShaders(
    ) = mapOf(
        "none" to none,
        "red" to red,
        "green" to green,
        "blue" to blue,
        "rgb vertical" to rgbVertical,
        "rgb horizontal" to rgbHorizontal,
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
    )

    fun getShadersWithTime() = mapOf(
        "none" to none,
        "ripple" to ripple,
        "liquid" to liquid,
        "bokeh" to bokeh,
        "flow field" to flowField,
        "flow field 2" to flowField2,
    )

    fun getShadersWithPointer() = mapOf(
        "ripple pointer" to ripplePointer,
        "expansive diffusion" to expansiveDiffusion,
        "expansive diffusion 2" to expansiveDiffusionTwo,
    )
}
