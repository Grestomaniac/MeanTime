package com.sillyapps.meantime

object AppConstants {
    const val DEFAULT_SCHEME_ID = 1

    const val UNCERTAIN = -2L
    const val NOT_ASSIGNED = -1

    const val NORMAL_INTERVAL = 1000L
    const val BATTERY_SAVING_INTERVAL = 10000L
    const val CRITICAL_TIME_REMAINED = 300000L

    const val SERVICE_MAIN_NOTIFICATION_CHANNEL = "DayServiceChannel"

    const val ALARM_DEFAULT_DURATION = 60000L
    const val VIBRATION_DEFAULT_DURATION = 3000L

    const val TIMER_CHECK_INTERVAL = 100000L

    const val DEFAULT_RINGTONE = "Default"
}

object AppBR {
    const val taskFinishedNaturally = 1001
    const val dayEnded = 1002
    const val dayPausedOrUnPaused = 1003
}