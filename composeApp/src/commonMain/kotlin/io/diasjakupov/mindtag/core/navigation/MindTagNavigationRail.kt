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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import io.diasjakupov.mindtag.core.designsystem.MindTagColors

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
        containerColor = MindTagColors.BottomNavBg,
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
                    selectedIconColor = MindTagColors.Primary,
                    selectedTextColor = MindTagColors.Primary,
                    unselectedIconColor = MindTagColors.TextSecondary,
                    unselectedTextColor = MindTagColors.TextSecondary,
                    indicatorColor = MindTagColors.Primary.copy(alpha = 0.12f),
                ),
            )
        }
    }
}
