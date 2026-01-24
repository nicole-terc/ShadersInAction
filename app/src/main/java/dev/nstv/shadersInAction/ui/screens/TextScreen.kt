package dev.nstv.shadersInAction.ui.screens

import android.graphics.RenderEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import dev.nstv.shadersInAction.ui.shaders.SimpleShaders
import dev.nstv.shadersInAction.ui.theme.Grid
import dev.nstv.shadersInAction.ui.theme.components.CheckBoxLabel
import dev.nstv.shadersInAction.ui.theme.components.DropDownWithArrows


@Composable
fun TextScreen(
    padding: PaddingValues,
    modifier: Modifier = Modifier
) {

    val shadersMap = SimpleShaders.getShaderMap()
    val shadersOptions = shadersMap.keys.toList()
    val shaders = shadersMap.values.toList()
    var selectedShaderIndex by remember { mutableIntStateOf(0) }
    var useBrush by remember { mutableStateOf(true) }

    Column(
        modifier
            .padding(padding)
            .padding(Grid.Two)
    ) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxSize(),
        ) {
            Text(
                text = "Screen A",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .graphicsLayer {
                        clip = true
                        if (!useBrush) {
                            renderEffect =
                                RenderEffect.createRuntimeShaderEffect(
                                    shaders[selectedShaderIndex],
                                    "composable"
                                )
                                    .asComposeRenderEffect()
                        }
                    }
                    .drawWithCache {
                        val selectedShader = shaders[selectedShaderIndex]
                        val brush = ShaderBrush(selectedShader)
                        brush
                        onDrawWithContent {
                            drawContent()
                            if (useBrush) {
                                drawRect(brush, blendMode = BlendMode.SrcAtop)
                            }
                        }
                    }
                    .align(Alignment.Center)

            )
        }
        DropDownWithArrows(
            modifier = Modifier.fillMaxWidth(),
            options = shadersOptions,
            selectedIndex = selectedShaderIndex,
            onSelectionChanged = { selectedShaderIndex = it },
            label = "Shader",
        )
        CheckBoxLabel(
            text = "Use Brush",
            checked = useBrush,
            onCheckedChange = { useBrush = it }
        )
    }
}
