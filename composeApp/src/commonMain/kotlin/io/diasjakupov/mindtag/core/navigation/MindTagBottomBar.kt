package io.diasjakupov.mindtag.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp

private val ActiveColor = Color(0xFF135BEC)
private val InactiveColor = Color(0xFF92A4C9)
private val BarBackground = Color(0xF0111722)

private data class BottomTab(
    val route: Route,
    val label: String,
    val icon: ImageVector,
)

private val tabs = listOf(
    BottomTab(Route.Library, "Library", Icons.Outlined.LocalLibrary),
    BottomTab(Route.Study, "Study", Icons.Outlined.EditNote),
)

@Composable
fun MindTagBottomBar(
    currentRoute: Route?,
    onTabSelected: (Route) -> Unit,
) {
    NavigationBar(
        containerColor = BarBackground,
    ) {
        tabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab.route) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        fontSize = 10.sp,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ActiveColor,
                    selectedTextColor = ActiveColor,
                    unselectedIconColor = InactiveColor,
                    unselectedTextColor = InactiveColor,
                    indicatorColor = ActiveColor.copy(alpha = 0.12f),
                ),
            )
        }
    }
}
