package com.stadiolinks.kitabu.ui.reservations

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stadiolinks.kitabu.R
import com.stadiolinks.kitabu.di.AppViewModelProvider
import com.stadiolinks.kitabu.domain.model.BookingStatus
import com.stadiolinks.kitabu.domain.model.Reservation
import com.stadiolinks.kitabu.ui.common.CollectUserMessages
import com.stadiolinks.kitabu.ui.components.BookingStatusBadge
import com.stadiolinks.kitabu.ui.components.DeadlineBadge
import com.stadiolinks.kitabu.ui.util.formatDisplayDate

/** Stateful entry point: connects the ViewModel to the stateless [ReservationsScreen]. */
@Composable
fun ReservationsRoute(
    snackbarHostState: SnackbarHostState,
    onBrowseCatalog: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReservationsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CollectUserMessages(viewModel.messages, snackbarHostState)

    ReservationsScreen(
        uiState = uiState,
        onRenew = viewModel::onRenew,
        onReturn = viewModel::onReturn,
        onCancel = viewModel::onCancel,
        onConfirmPickup = viewModel::onConfirmPickup,
        onBrowseCatalog = onBrowseCatalog,
        modifier = modifier
    )
}

@Composable
fun ReservationsScreen(
    uiState: ReservationsUiState,
    onRenew: (Reservation) -> Unit,
    onReturn: (Reservation) -> Unit,
    onCancel: (Reservation) -> Unit,
    onConfirmPickup: (Reservation) -> Unit,
    onBrowseCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Cancelling deletes the booking, so ask for confirmation first.
    var cancelTarget by remember { mutableStateOf<Reservation?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        ReservationsHeader(count = uiState.items.size, isLoading = uiState.isLoading)

        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            uiState.items.isEmpty() -> EmptyReservations(onBrowseCatalog)

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = uiState.items, key = { it.reservation.bookingId }) { item ->
                    ReservationCard(
                        item = item,
                        onRenew = { onRenew(item.reservation) },
                        onReturn = { onReturn(item.reservation) },
                        onCancel = { cancelTarget = item.reservation },
                        onConfirmPickup = { onConfirmPickup(item.reservation) }
                    )
                }
            }
        }
    }

    cancelTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { cancelTarget = null },
            title = { Text(stringResource(R.string.cancel_dialog_title)) },
            text = { Text(stringResource(R.string.cancel_dialog_body, target.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCancel(target)
                        cancelTarget = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.cancel_dialog_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { cancelTarget = null }) {
                    Text(stringResource(R.string.cancel_dialog_dismiss))
                }
            }
        )
    }
}

@Composable
private fun ReservationsHeader(count: Int, isLoading: Boolean) {
    val resources = LocalContext.current.resources
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.reservations_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        if (!isLoading && count > 0) {
            Text(
                text = resources.getQuantityString(R.plurals.reservations_summary, count, count),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReservationCard(
    item: ReservationItemUi,
    onRenew: () -> Unit,
    onReturn: () -> Unit,
    onCancel: () -> Unit,
    onConfirmPickup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reservation = item.reservation
    val isActive = reservation.status == BookingStatus.ACTIVE
    val isPending = reservation.status == BookingStatus.PENDING
    val buttonPadding = PaddingValues(horizontal = 8.dp)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reservation.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = reservation.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BookingStatusBadge(status = reservation.status)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                LabeledDate(
                    label = stringResource(R.string.label_reserved_on),
                    value = formatDisplayDate(reservation.reservedOn)
                )
                LabeledDate(
                    label = stringResource(R.string.label_return_by),
                    value = formatDisplayDate(reservation.returnDeadline)
                )
            }

            DeadlineBadge(text = daysRemainingText(item.daysRemaining), isOverdue = item.isOverdue)

            if (isPending) {
                Button(
                    onClick = onConfirmPickup,
                    enabled = !item.isBusy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.action_confirm_pickup))
                }
            }

            // All three lifecycle controls are always visible; each is enabled only when valid.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onRenew,
                    enabled = isActive && !item.isBusy,
                    contentPadding = buttonPadding,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.action_renew), maxLines = 1)
                }
                FilledTonalButton(
                    onClick = onReturn,
                    enabled = isActive && !item.isBusy,
                    contentPadding = buttonPadding,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.action_return), maxLines = 1)
                }
                OutlinedButton(
                    onClick = onCancel,
                    enabled = isPending && !item.isBusy,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    contentPadding = buttonPadding,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.action_cancel), maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun LabeledDate(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun daysRemainingText(days: Long): String {
    val resources = LocalContext.current.resources
    return when {
        days < 0 -> {
            val n = (-days).toInt()
            resources.getQuantityString(R.plurals.days_overdue, n, n)
        }
        days == 0L -> stringResource(R.string.days_due_today)
        else -> {
            val n = days.toInt()
            resources.getQuantityString(R.plurals.days_left, n, n)
        }
    }
}

@Composable
private fun EmptyReservations(onBrowseCatalog: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.reservations_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.reservations_empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBrowseCatalog) {
                Text(stringResource(R.string.reservations_browse))
            }
        }
    }
}
