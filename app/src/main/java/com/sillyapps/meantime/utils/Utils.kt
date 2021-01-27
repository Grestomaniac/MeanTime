package com.sillyapps.meantime.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.sillyapps.meantime.PreferencesKeys
import com.sillyapps.meantime.R
import com.sillyapps.meantime.hideKeyBoard
import timber.log.Timber
import java.util.*


fun getLocalCurrentTimeMillis(): Long {
    val tz = TimeZone.getDefault()
    val currentTimeInUTC = System.currentTimeMillis()
    val offsetFromUTC = tz.getOffset(currentTimeInUTC)

    return currentTimeInUTC + offsetFromUTC
}

fun convertToMillis(hours: Int, minutes: Int, seconds: Int = 0): Long {
    return ((hours*60L + minutes)*60 + seconds)*1000L
}

fun convertMillisToStringFormat(millis: Long): String {
    val overallMinutes = millis / 60000
    val minutes = overallMinutes % 60
    val overallHours = overallMinutes / 60
    val hours = overallHours % 24

    return formatIfNeeded(hours.toInt(), minutes.toInt())
}

fun convertMillisToStringFormatWithSeconds(millis: Long): String {
    val overallSeconds = millis / 1000
    val seconds = overallSeconds % 60

    val overallMinutes = overallSeconds / 60
    val minutes = overallMinutes % 60

    val overallHours = overallMinutes / 60
    val hours = overallHours % 24

    var formattedSeconds = ":$seconds"
    if (seconds < 10)
        formattedSeconds = ":0$seconds"

    return formatIfNeeded(hours.toInt(), minutes.toInt(), formattedSeconds)
}

fun formatIfNeeded(hours: Int, minutes: Int, secondsFormatted: String = ""): String {
    var stringHours = hours.toString()
    var stringMinutes = minutes.toString()

    if (hours < 10) {
        stringHours = "0$hours"
    }
    if (minutes < 10) {
        stringMinutes = "0$minutes"
    }
    return "$stringHours:$stringMinutes$secondsFormatted"
}

fun tintMenuIcons(items: Sequence<MenuItem>, context: Context) {
    for (item in items) {
        item.icon?.apply {
            item.icon.setTintMode(PorterDuff.Mode.SRC_ATOP)
            item.icon.setTint(ContextCompat.getColor(context, R.color.primaryTextColor))
        }
    }
}

fun formatString(string: String): String {
    return string.toLowerCase(Locale.getDefault())
}

fun removeExtraSpaces(string: String): String {
    val sb = StringBuilder()
    var haveSpaceBefore = false
    for (c in string) {
        if (c != ' ') {
            sb.append(c)
            haveSpaceBefore = false
        }
        else if (!haveSpaceBefore) {
            sb.append(c)
            haveSpaceBefore = true
        }
    }

    if (sb.last() == ' ') sb.deleteCharAt(sb.lastIndex)
    return sb.toString()
}

fun Fragment.showInfoToUser(messageId: Int) {
    val message = getString(messageId)
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.setDarkThemeIfNeeded() {
    val systemNightModeEnabled = resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    if (systemNightModeEnabled) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
    else {
        val preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(
            applicationContext
        )
        val appNightModeEnabled = preferences.getBoolean(PreferencesKeys.NIGHT_MODE_IS_ON, false)
        if (appNightModeEnabled)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}

fun View.getActivity(): AppCompatActivity? {
    var context = context
    while (context is ContextWrapper) {
        if (context is AppCompatActivity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

fun formatTime(value: Int): String {
    return if (value > 9) value.toString()
    else "0$value"
}