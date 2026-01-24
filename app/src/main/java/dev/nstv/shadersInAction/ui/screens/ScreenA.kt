package dev.nstv.shadersInAction.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

private const val SHADER = """
    half4 main(float2 fragCoord) {
      return half4(1,0,0,1);
    } 
"""


@Composable
fun ScreenA(padding: PaddingValues) {
    val fixedColorShader =  (SHADER)

    Box(Modifier.fillMaxSize().padding(padding)) {
        Text(
            text = "Screen A",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
