package dev.nstv.shadersInAction.ui.screens

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import dev.nstv.shadersInAction.ui.FullAppShader
import dev.nstv.shadersInAction.ui.components.BoxWithTime
import dev.nstv.shadersInAction.ui.theme.Grid
import dev.nstv.shadersInAction.ui.theme.TileColor

private val shader = RuntimeShader(SHADER_SIX)

@Composable
fun BoringScreen() {
    BoxWithTime(
        modifier = Modifier.fillMaxSize()
    ) { time ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (!FullAppShader) {
                        shader.setFloatUniform("time", time)
                        shader.setFloatUniform("size", floatArrayOf(size.width, size.height))
                        renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "composable")
                            .asComposeRenderEffect()
                    }
                }
        ) {
            LazyColumn {
                items(30) { index ->
                    BoringItem(index)
                }
            }
        }
    }
}

@Composable
fun BoringItem(
    index: Int,
    modifier: Modifier = Modifier

) {
    val color by remember { mutableStateOf(TileColor.list[index % TileColor.list.size]) }
    Row(
        modifier = modifier
            .padding(Grid.Half)
            .height(intrinsicSize = IntrinsicSize.Max)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(Grid.One))
            .padding(Grid.One),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .background(color, shape = RoundedCornerShape(Grid.Half))
                .size(Grid.Six)
        )
        Spacer(Modifier.size(Grid.One))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.SpaceEvenly) {
            Text(
                text = "Title $index",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Long subtitle here",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
