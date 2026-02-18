package io.diasjakupov.mindtag.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp

private val ActiveColor = Color(0xFF135BEC)
private val InactiveColor = Color(0xFF92A4C9)
private val RailBackground = Color(0xF0111722)

private data class RailTab(
    val route: Route,
    val label: String,
    val icon: ImageVector,
)

private val tabs = listOf(
    RailTab(Route.Library, "Library", Icons.Outlined.LocalLibrary),
    RailTab(Route.Study, "Study", Icons.Outlined.EditNote),
)

@Composable
fun MindTagNavigationRail(
    currentRoute: Route?,
    onTabSelected: (Route) -> Unit,
) {
    NavigationRail(
        containerColor = RailBackground,
    ) {
        tabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationRailItem(
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
                colors = NavigationRailItemDefaults.colors(
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
