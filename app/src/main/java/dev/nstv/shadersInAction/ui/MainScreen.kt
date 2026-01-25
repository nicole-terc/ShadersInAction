package dev.nstv.shadersInAction.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dev.nstv.shadersInAction.R
import dev.nstv.shadersInAction.ui.screens.ImageScreen
import dev.nstv.shadersInAction.ui.screens.TextScreen
import dev.nstv.shadersInAction.ui.theme.Grid
import kotlinx.coroutines.launch

// Start Config
const val HideOptions = false
const val Sheep = false
// End Config

enum class DrawerDestination(val title: String) {
    TextScreen("Text Screen"),
    ImageScreen("Image Screen"),

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {
    val backStack = remember { listOf<Any>(DrawerDestination.ImageScreen).toMutableStateList() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentDestination = backStack.lastOrNull() as? DrawerDestination
        ?: DrawerDestination.TextScreen

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                currentDestination = currentDestination,
                onDestinationSelected = { destination ->
                    if (destination != currentDestination) {
                        backStack.resetTo(destination)
                    }
                    scope.launch { drawerState.close() }
                },
            )
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentDestination.title) },
                    navigationIcon = {
                        Icon(
                            painterResource(R.drawable.menu),
                            contentDescription = "Open navigation drawer",
                            modifier = Modifier
                                .size(Grid.Three)
                                .clickable { scope.launch { drawerState.open() } }
                        )
                    },
                )
            },
        ) { padding ->
            NavDisplay(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                backStack = backStack,
                onBack = { backStack.popLast() },
                entryProvider = entryProvider {
                    entry(DrawerDestination.TextScreen) { TextScreen() }
                    entry(DrawerDestination.ImageScreen) { ImageScreen() }
                }
            )
        }
    }
}

@Composable
private fun DrawerContent(
    currentDestination: DrawerDestination,
    onDestinationSelected: (DrawerDestination) -> Unit,
) {
    ModalDrawerSheet {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(Grid.Two)
        ) {
            item("header") {
                Text(
                    text = "Screens",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            items(DrawerDestination.entries.size) { index ->
                NavigationDrawerItem(
                    label = { Text(DrawerDestination.entries[index].title) },
                    selected = currentDestination == DrawerDestination.entries[index],
                    onClick = { onDestinationSelected(DrawerDestination.entries[index]) },
                )
            }
        }
    }
}

private fun SnapshotStateList<Any>.resetTo(destination: DrawerDestination) {
    clear()
    add(destination)
}

private fun SnapshotStateList<Any>.popLast() {
    if (size > 1) {
        removeAt(lastIndex)
    }
}
