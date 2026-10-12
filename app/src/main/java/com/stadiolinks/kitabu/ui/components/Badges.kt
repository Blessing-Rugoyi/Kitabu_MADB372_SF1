package com.stadiolinks.kitabu.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stadiolinks.kitabu.R
import com.stadiolinks.kitabu.domain.model.BookingStatus
import com.stadiolinks.kitabu.ui.theme.AmberContainer
import com.stadiolinks.kitabu.ui.theme.AvailableContainer
import com.stadiolinks.kitabu.ui.theme.BorrowedContainer
import com.stadiolinks.kitabu.ui.theme.OnAmberContainer
import com.stadiolinks.kitabu.ui.theme.OnAvailable
import com.stadiolinks.kitabu.ui.theme.OnBorrowed
import com.stadiolinks.kitabu.ui.theme.OnOverdue
import com.stadiolinks.kitabu.ui.theme.OverdueContainer
import androidx.compose.ui.res.stringResource

/** Pill with an icon and a text label, so meaning is never conveyed by colour alone. */
@Composable
private fun Pill(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/** Green "Available" / orange "Borrowed" badge for catalogue cards. */
@Composable
fun AvailabilityBadge(isAvailable: Boolean, modifier: Modifier = Modifier) {
    Pill(
        text = stringResource(if (isAvailable) R.string.badge_available else R.string.badge_borrowed),
        icon = if (isAvailable) Icons.Default.Check else Icons.Default.Info,
        containerColor = if (isAvailable) AvailableContainer else BorrowedContainer,
        contentColor = if (isAvailable) OnAvailable else OnBorrowed,
        modifier = modifier
    )
}

/** "Pending pickup" / "Active" label for reservation cards. */
@Composable
fun BookingStatusBadge(status: BookingStatus, modifier: Modifier = Modifier) {
    val isActive = status == BookingStatus.ACTIVE
    Pill(
        text = stringResource(if (isActive) R.string.status_active else R.string.status_pending),
        icon = if (isActive) Icons.Default.Check else Icons.Default.Info,
        containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

/** Amber deadline chip ("N days left"); turns red with a warning icon when overdue. */
@Composable
fun DeadlineBadge(text: String, isOverdue: Boolean, modifier: Modifier = Modifier) {
    Pill(
        text = text,
        icon = if (isOverdue) Icons.Default.Warning else Icons.Default.DateRange,
        containerColor = if (isOverdue) OverdueContainer else AmberContainer,
        contentColor = if (isOverdue) OnOverdue else OnAmberContainer,
        modifier = modifier
    )
}
