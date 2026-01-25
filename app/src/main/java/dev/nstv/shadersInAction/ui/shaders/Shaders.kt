package dev.nstv.shadersInAction.ui.shaders

import android.graphics.RuntimeShader

object Shaders {

    // Runtime Shaders
    val none = RuntimeShader(NONE)
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
    val ripple = RuntimeShader(RIPPLE).apply {
        setFloatUniform("time", 0f)
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
    )
}
