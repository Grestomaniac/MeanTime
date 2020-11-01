package com.sillyapps.meantime.data

import androidx.databinding.BaseObservable

class TimeWithSeconds(task: Task): BaseObservable() {
    var hours: Int
    var minutes: Int
    var seconds: Int

    init {
        val overallSeconds = (task.duration / 1000).toInt()
        seconds = overallSeconds % 60

        val overallMinutes = overallSeconds / 60
        minutes = overallMinutes % 60

        val overallHours = overallMinutes / 60
        hours = overallHours % 24
    }
}