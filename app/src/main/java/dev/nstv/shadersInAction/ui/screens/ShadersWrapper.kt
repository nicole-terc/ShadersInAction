package dev.nstv.shadersInAction.ui.screens

import android.graphics.RuntimeShader
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.nstv.shadersInAction.ui.shaders.Shaders
import dev.nstv.shadersInAction.ui.theme.Grid
import dev.nstv.shadersInAction.ui.theme.components.DropDownWithArrows

@Composable
fun ShadersWrapper(
    modifier: Modifier = Modifier,
    shadersMap: Map<String, RuntimeShader> = Shaders.getRenderShaders(),
    content: @Composable BoxScope.(RuntimeShader) -> Unit,
) {
    val shadersOptions = shadersMap.keys.toList()
    val shaders = shadersMap.values.toList()
    var selectedShaderIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier
            .fillMaxSize()
    ) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            content(shaders[selectedShaderIndex])
        }
        DropDownWithArrows(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Grid.Two),
            options = shadersOptions,
            selectedIndex = selectedShaderIndex,
            onSelectionChanged = { selectedShaderIndex = it },
            label = "Shader",
        )
    }
}

