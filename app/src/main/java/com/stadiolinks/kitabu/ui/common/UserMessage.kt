package com.stadiolinks.kitabu.ui.common

import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.stadiolinks.kitabu.R
import com.stadiolinks.kitabu.domain.model.LibraryError
import kotlinx.coroutines.flow.Flow

/** A one-off message for the snackbar, described by string resource so it stays localisable. */
data class UserMessage(
    @StringRes val resId: Int,
    val args: List<Any> = emptyList()
)

fun LibraryError.toUserMessage(): UserMessage = UserMessage(
    when (this) {
        LibraryError.BOOK_NOT_FOUND -> R.string.error_book_not_found
        LibraryError.BOOK_UNAVAILABLE -> R.string.error_book_unavailable
        LibraryError.BOOKING_NOT_FOUND -> R.string.error_booking_not_found
        LibraryError.INVALID_STATE -> R.string.error_invalid_state
        LibraryError.OVERDUE -> R.string.error_overdue
        LibraryError.MAX_RENEWALS_REACHED -> R.string.error_max_renewals
        LibraryError.INVALID_DEADLINE -> R.string.error_invalid_deadline
        LibraryError.STORAGE -> R.string.error_storage
    }
)

/** Shows each emitted [UserMessage] in the snackbar, replacing any message still on screen. */
@Composable
fun CollectUserMessages(messages: Flow<UserMessage>, snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    LaunchedEffect(messages) {
        messages.collect { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(context.getString(message.resId, *message.args.toTypedArray()))
        }
    }
}
