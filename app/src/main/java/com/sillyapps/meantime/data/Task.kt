package com.sillyapps.meantime.data

import android.annotation.SuppressLint
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.sillyapps.meantime.BR
import com.sillyapps.meantime.UNCERTAIN
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

data class Task(
    var startTime: Long = 0L,

    val name: String = "",
    val duration: Long = 0L,
    val vibrationOn: Boolean,
    val soundOn: Boolean,
    val sound: String
) {
    var uiStartTime: String = convertMillisToStringFormat(startTime)

    fun addCurrentDayOffset(currentTime: Long) {
        uiStartTime = formatter.format(Date(startTime + currentTime))
    }

    fun updateUI() {
        uiStartTime = convertMillisToStringFormat(startTime)
    }

    fun getNextStartTime(): Long {
        return if (duration == UNCERTAIN || startTime == UNCERTAIN) {
            UNCERTAIN
        }
        else {
            startTime + duration
        }
    }

    fun updateStartTime(time: Long) {
        startTime = time
        updateUI()
    }

    companion object {
        @SuppressLint("SimpleDateFormat")
        val formatter = SimpleDateFormat("HH:mm")

        fun createFromEditableTask(editableTask: EditableTask): Task {
            editableTask.apply {
                return Task(
                    startTime, editableName,
                    editableDuration, editableVibrationOn,
                    editableSoundOn, editableSound) }
        }
    }
}

class EditableTask(
    var startTime: Long = 0L,
    var editableName: String = "",
    var editableDuration: Long = 0L,
    var editableVibrationOn: Boolean = true,
    var editableSoundOn: Boolean = true,
    sound: String = "default"): BaseObservable() {

    @Bindable
    var editableUiDuration: String = convertMillisToStringFormat(editableDuration)
        set(value) {
            field = value
            notifyPropertyChanged(BR.editableUiDuration)
        }

    @Bindable
    var editableSound: String = sound
        set(value) {
            field = value
            notifyPropertyChanged(BR.editableSound)
        }

    fun setDuration(hours: Int, minutes: Int) {
        editableDuration = (hours*60L + minutes)*60000L
        editableUiDuration = formatIfNeeded(hours, minutes)
    }


    fun isDataValid(): WhatIsWrong {
        if (editableName == "") return WhatIsWrong.NAME
        if (editableDuration == 0L) return WhatIsWrong.DURATION

        return WhatIsWrong.NOTHING
    }

    enum class WhatIsWrong {
        NAME, DURATION, NOTHING
    }

    companion object {
        fun copyFromExistingTask(task: Task): EditableTask {
            task.apply {
                return EditableTask(task.startTime, name, duration, vibrationOn, soundOn, sound)
            }
        }
    }
}

fun convertMillisToStringFormat(millis: Long): String {
    val overallMinutes = millis / 60000
    val minutes = overallMinutes % 60
    val overallHours = overallMinutes / 60
    val hours = overallHours % 24

    return formatIfNeeded(hours.toInt(), minutes.toInt())
}

fun formatIfNeeded(hours: Int, minutes: Int): String {
    var stringHours = hours.toString()
    var stringMinutes = minutes.toString()

    if (hours < 10) {
        stringHours = "0$hours"
    }
    if (minutes < 10) {
        stringMinutes = "0$minutes"
    }
    return "$stringHours:$stringMinutes"
}