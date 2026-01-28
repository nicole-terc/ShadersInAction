package dev.nstv.shadersInAction.ui.screens

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import dev.nstv.shadersInAction.R
import dev.nstv.shadersInAction.ui.components.BoxWithTime
import dev.nstv.shadersInAction.ui.shaders.FRACTAL
import dev.nstv.shadersInAction.ui.shaders.NOISE_GRID
import org.intellij.lang.annotations.Language

const val SHADER = """
    uniform shader composable;
    uniform float2 size;
    uniform float time;
    
    half4 main(float2 fragCoord) {
        if(fragCoord.x <= size.x / 2.0){
            // RED 
            return half4(1.0, 0.0, 0.0, 1.0);
        }

        // BLUE 
        return half4(0.0, 0.0, 1.0, 1.0);
    }
"""

const val SHADER_TWO = """
    uniform shader composable;
    uniform float2 size;
    uniform float time;
    
    half4 main(float2 fragCoord) {
       half4 base = composable.eval(fragCoord);
       
        if(fragCoord.x <= size.x / 2.0){
            // RED 
            return half4(1.0, base.gba);
       }
       
        // BLUE 
        return half4(base.rg, 1.0, base.a);
    }
"""

const val SHADER_THREE = """
    uniform shader composable;
    uniform float2 size;
    uniform float time;
    
    half4 main(float2 fragCoord) {
       half4 base = composable.eval(fragCoord);
       half4 red = half4(1.0, base.gba);
       half4 blue = half4(base.rg, 1.0, base.a);
       
       return mix(red, blue, fragCoord.x / size.x);
    }
"""

const val SHADER_FOUR = """
    uniform shader composable;
    uniform float2 size;
    uniform float time;
    
    half4 main(float2 fragCoord) {
       half4 base = composable.eval(fragCoord);
       half4 red = half4(1.0, base.gba);
       half4 blue = half4(base.rg, 1.0, base.a);
       
       return mix(red, blue, sin(fragCoord.x / size.x + time));
    }
"""

const val SHADER_FIVE = """
    uniform shader composable;
    uniform float2 size;
    uniform float time;
    
    const float amplitude = 0.02;   
    const float frequency = 10.0;
    
    half4 main(float2 fragCoord) {
        float2 uv = fragCoord/size;

        float wave = sin((uv.y * frequency * 6.2831853) + time*2.0);
        uv.x += wave * amplitude;

        return composable.eval(uv * size);
        
    }
"""

@Language("AGSL")
const val SHADER_SIX = """
    uniform shader composable;
    uniform float2 size;
    uniform float time;
    
    const float amplitude = 0.02;   
    const float frequency = 10.0;
    
    half4 main(float2 fragCoord) {
        float2 uv = fragCoord/size;

        // vertical wave
        float wave = sin((uv.y * frequency * 6.2831853) + time*2.0);
        uv.x += wave * amplitude;

        // horizontal wave
//        wave = sin((uv.x * frequency * 6.2831853) + time*2.0);
//        uv.y += wave * amplitude;

        half4 base = composable.eval(uv * size);
        
        half4 red = half4(1.0, base.gba);
        half4 blue = half4(base.rg, 1.0, base.a);
       
        return mix(red, blue, sin(uv.x + time));
        
    }
"""

private val shader = RuntimeShader(NOISE_GRID)

@Composable
fun DemoScreen() {
    // BRUSH
    val shaderBrush = ShaderBrush(shader)
    BoxWithTime(
        Modifier
            .fillMaxSize()
//            .drawWithCache {
//                shader.setFloatUniform("time", 0f)
//                shader.setFloatUniform("size", floatArrayOf(size.width, size.height))
//                onDrawBehind {
//                    drawRect(shaderBrush)
//                }
//            }
    ) { time ->
//        Text(
//            text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur suscipit orci id sem convallis gravida. Maecenas fermentum finibus est. \n\nLook at my super shader! :D",
//            style = MaterialTheme.typography.headlineLarge,
//            textAlign = TextAlign.Center,
//            fontSize = 50.sp,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier
//                .align(Alignment.Center)
//                .alpha(0.9f)
//                .drawWithCache {
//                    shader.setFloatUniform("time", 0f)
//                    shader.setFloatUniform("size", floatArrayOf(size.width, size.height))
//                    onDrawWithContent {
//                        drawContent()
//                        drawRect(shaderBrush, blendMode = BlendMode.SrcAtop)
//                    }
//                },
//        )

//        Image(
//            painter = painterResource(R.drawable.sheep),
//            contentDescription = "Sheep",
//            contentScale = ContentScale.Crop,
//            modifier = Modifier
//                .fillMaxSize()
//                .align(Alignment.Center)
//                .alpha(0.9f)
//                .drawWithCache {
//                    shader.setFloatUniform("time", 0f)
//                    shader.setFloatUniform("size", floatArrayOf(size.width, size.height))
//                    onDrawWithContent {
//                        drawContent()
//                        drawRect(
//                            brush = shaderBrush,
//                            blendMode = BlendMode.Color
//                        )
//                    }
//                },
//        )

        // RENDER EFFECT
        Image(
            painter = painterResource(R.drawable.sheep),
            contentDescription = "Sheep",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    shader.setFloatUniform("time", time)
                    shader.setFloatUniform("size", floatArrayOf(size.width, size.height))
                    renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "composable")
                        .asComposeRenderEffect()
                }
        )

    }
}
