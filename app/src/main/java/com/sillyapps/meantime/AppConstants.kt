package com.sillyapps.meantime

import androidx.databinding.Bindable

object AppConstants {
    const val UNCERTAIN = -2L
    const val NOT_ASSIGNED = -1

    const val SERVICE_MAIN_NOTIFICATION_CHANNEL = "DayServiceChannel"

    const val ALARM_DEFAULT_DURATION = 60000L
    const val VIBRATION_DEFAULT_DURATION = 3000L

    const val DEFAULT_RINGTONE = "android.resource://com.sillyapps.meantime/${R.raw.simple_jam}"
}

object AppBR {
    const val taskFinishedNaturally = 1001
}