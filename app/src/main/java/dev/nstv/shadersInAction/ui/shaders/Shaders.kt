package dev.nstv.shadersInAction.ui.shaders

import android.graphics.RuntimeShader

object Shaders {

    // Runtime Shaders
    val none = RuntimeShader(NONE).apply {
        setFloatUniform("time", 0f)
    }
    val red = RuntimeShader(RED)
    val green = RuntimeShader(GREEN)
    val blue = RuntimeShader(BLUE)
    val chromaticAberration = RuntimeShader(CHROMATIC_ABERRATION).apply {
        setFloatUniform("distortion", 30f)
    }
    val vignette = RuntimeShader(VIGNETTE)
    val bokeh = RuntimeShader(BOKEH).apply {
        setFloatUniform("time", 0f)
    }
    val blur = RuntimeShader(BLUR)
    val frostedGlass = RuntimeShader(FROSTED_GLASS)
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


    fun getRenderShaders(
    ) = mapOf(
        "none" to none,
        "red" to red,
        "green" to green,
        "blue" to blue,
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
}
