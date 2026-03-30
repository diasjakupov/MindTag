package io.diasjakupov.mindtag.core.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School

/**
 * Centralized icon references for MindTag.
 *
 * Uses material-icons-extended which is already included as a dependency.
 * Icons that require Material Symbols (e.g. hub, description, calendar_month,
 * edit_note, local_library, auto_awesome, electric_bolt,
 * local_fire_department, etc.) should be added as custom vector assets
 * when those screens are built.
 */
object MindTagIcons {
    // Navigation
    val Search = Icons.Filled.Search

    // Actions
    val Add = Icons.Filled.Add
    val Close = Icons.Filled.Close
    val ArrowBack = Icons.AutoMirrored.Filled.ArrowBack
    val ArrowForward = Icons.AutoMirrored.Filled.ArrowForward
    val Check = Icons.Filled.Check
    val Edit = Icons.Filled.Edit
    val Delete = Icons.Filled.Delete
    val Share = Icons.Filled.Share
    val MoreHoriz = Icons.Filled.MoreHoriz
    val Remove = Icons.Filled.Remove

    // Content-specific
    val Schedule = Icons.Outlined.Schedule
    val Analytics = Icons.Outlined.Analytics
    val AutoAwesome = Icons.Outlined.AutoAwesome
    val ExpandMore = Icons.Filled.ExpandMore
    val MenuBook = Icons.AutoMirrored.Outlined.MenuBook
    val School = Icons.Outlined.School
    val Headphones = Icons.Outlined.Headphones
    val History = Icons.Outlined.History
}
