package com.globuslens.utils

import android.content.Context
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

/**
 * Remembers the current LifecycleOwner
 */
@Composable
fun rememberLifecycleOwner(): LifecycleOwner {
    val lifecycleOwner = LocalLifecycleOwner.current
    return remember { lifecycleOwner }
}

/**
 * Launches a flow when the lifecycle is at least STARTED
 */
fun <T> Flow<T>.launchWhenStarted(
    lifecycleOwner: LifecycleOwner,
    coroutineBody: suspend (T) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { value ->
            coroutineBody(value)
        }
    }
}

/**
 * Checks if the LazyList is scrolled to the end
 */
fun LazyListState.isScrolledToEnd(): Boolean = layoutInfo.totalItemsCount > 0 &&
        layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1

/**
 * Capitalizes each word in a string (fix for deprecated capitalize())
 * Example: "hello world" -> "Hello World"
 */
fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { word ->
        word.replaceFirstChar {
            if (it.isLowerCase()) {
                it.titlecase(Locale.getDefault())
            } else {
                it.toString()
            }
        }
    }
}

/**
 * Capitalizes the first letter of a string (fix for deprecated capitalize())
 * Example: "hello" -> "Hello"
 */
fun String.capitalizeFirst(): String {
    return replaceFirstChar {
        if (it.isLowerCase()) {
            it.titlecase(Locale.getDefault())
        } else {
            it.toString()
        }
    }
}

/**
 * Checks if a string is a valid product name (not too short)
 */
fun String.isValidProductName(): Boolean = length >= Constants.MIN_TEXT_LENGTH

/**
 * Formats a Double as a currency string
 */
fun Double.formatPrice(currencyCode: String = "USD"): String {
    return try {
        NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance(currencyCode)
        }.format(this)
    } catch (e: Exception) {
        // Fallback formatting if currency is invalid
        String.format("%.2f %s", this, currencyCode)
    }
}

/**
 * Shows a Toast message in Compose
 */
@Composable
fun ShowToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(LocalContext.current, message, duration).show()
}

/**
 * Shows a Toast message from ViewModel or non-Compose parts
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Converts a String to an Enum with a default value if conversion fails
 */
inline fun <reified T : Enum<T>> String.toEnumOrDefault(default: T): T {
    return try {
        enumValueOf<T>(this.uppercase(Locale.getDefault()))
    } catch (e: IllegalArgumentException) {
        default
    }
}

/**
 * Truncates a string to a maximum length and adds ellipsis if needed
 */
fun String.truncate(maxLength: Int, ellipsis: String = "..."): String {
    return if (length <= maxLength) this else take(maxLength) + ellipsis
}

/**
 * Checks if a string is a valid email
 */
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

/**
 * Formats a date to a readable string
 */
fun Date.formatDate(pattern: String = "MMM dd, yyyy"): String {
    val format = java.text.SimpleDateFormat(pattern, Locale.getDefault())
    return format.format(this)
}

/**
 * Converts a list to a comma-separated string
 */
fun List<String>.toCommaSeparatedString(): String {
    return joinToString(", ")
}

/**
 * Safe division that returns 0 if divisor is 0
 */
fun Int.safeDivide(divisor: Int): Int {
    return if (divisor == 0) 0 else this / divisor
}

/**
 * Safe division that returns 0.0 if divisor is 0
 */
fun Double.safeDivide(divisor: Double): Double {
    return if (divisor == 0.0) 0.0 else this / divisor
}

/**
 * Extension function to run a block of code with a delay
 */
suspend fun <T> withDelay(delayMillis: Long, block: suspend () -> T): T {
    kotlinx.coroutines.delay(delayMillis)
    return block()
}

/**
 * Extension function to check if a string contains any of the given keywords
 */
fun String.containsAny(keywords: List<String>, ignoreCase: Boolean = true): Boolean {
    return keywords.any { this.contains(it, ignoreCase) }
}

/**
 * Extension function to mask a string (e.g., for sensitive data)
 */
fun String.mask(startKeep: Int = 0, endKeep: Int = 0, maskChar: Char = '*'): String {
    if (length <= startKeep + endKeep) return this

    val start = take(startKeep)
    val end = takeLast(endKeep)
    val masked = maskChar.toString().repeat(length - startKeep - endKeep)

    return start + masked + end
}

/**
 * Extension function to get the file extension from a path
 */
fun String.getFileExtension(): String {
    return substringAfterLast('.', "")
}

/**
 * Extension function to check if a string is a valid URL
 */
fun String.isValidUrl(): Boolean {
    return try {
        java.net.URL(this)
        true
    } catch (e: java.net.MalformedURLException) {
        false
    }
}

/**
 * Extension function to convert a string to title case
 */
fun String.toTitleCase(): String {
    return split(" ").joinToString(" ") { word ->
        word.lowercase(Locale.getDefault()).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }
}