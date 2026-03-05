package com.globuslens.utils


import android.widget.Toast
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@Composable
fun rememberLifecycleOwner(): LifecycleOwner {
    val lifecycleOwner = LocalLifecycleOwner.current
    return remember { lifecycleOwner }

}

fun <T> Flow<T>.launchWhenStarted(lifecycleOwner: LifecycleOwner, coroutineBody: suspend (T) -> Unit) {
    lifecycleOwner.lifecycleScope.launch {
        flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { value ->
            coroutineBody(value)
        }
    }
}

fun LazyListState.isScrolledToEnd() = layoutInfo.totalItemsCount > 0 &&
        layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1

fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.capitalize() }

fun String.isValidProductName(): Boolean = length >= Constants.MIN_TEXT_LENGTH

fun Double.formatPrice(currencyCode: String = "USD"): String {
    return NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance(currencyCode)
    }.format(this)
}

@Composable
fun ShowToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(LocalContext.current, message, duration).show()
}

inline fun <reified T : Enum<T>> String.toEnumOrDefault(default: T): T {
    return try {
        enumValueOf<T>(this)
    } catch (e: IllegalArgumentException) {
        default
    }
}