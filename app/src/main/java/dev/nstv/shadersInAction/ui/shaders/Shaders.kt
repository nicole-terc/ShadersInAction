package dev.nstv.shadersInAction.ui.shaders

import android.graphics.RuntimeShader

object Shaders {

    // Runtime Shaders
    private val none = RuntimeShader(NONE)
    private val red = RuntimeShader(RED)
    private val green = RuntimeShader(GREEN)
    private val blue = RuntimeShader(BLUE)
    private val chromaticAberration = RuntimeShader(CHROMATIC_ABERRATION).apply {
        setFloatUniform("distortion", 30f)
    }

    fun getRenderShaders() = mapOf(
        "red" to red,
        "green" to green,
        "blue" to blue,
        "chromatic aberration" to chromaticAberration,


        "none" to none,
    )
}
