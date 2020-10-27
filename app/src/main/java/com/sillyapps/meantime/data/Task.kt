package com.sillyapps.meantime.data

import android.annotation.SuppressLint
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.sillyapps.meantime.*
import timber.log.Timber
import java.text.SimpleDateFormat

data class Task(
    var startTime: Long = 0L,

    val name: String = "",
    val duration: Long = 0L,
    val vibrationOn: Boolean,
    val soundOn: Boolean,
    val sound: String
) {
    var uiStartTime: String = convertMillisToStringFormat(startTime)

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

class RunningTask(
    startTime: Long,

    var name: String = "",
    private val originalDuration: Long = 0L,

    private val originalVibrationOn: Boolean,
    private val originalSoundOn: Boolean,
    val sound: String
): BaseObservable() {
    var startTime: Long = startTime
        set(value) {
            field = value
            uiStartTime = convertMillisToStringFormat(value)
        }

    @Bindable
    var uiStartTime: String = convertMillisToStringFormat(startTime)
        set(value) {
            field = value
            notifyPropertyChanged(BR.uiStartTime)
        }

    var duration: Long = originalDuration
        set(value) {
            field = value
        }

    var soundOn = originalSoundOn
    var vibrationOn = originalVibrationOn

    var state: State = State.WAITING
        set(value) {
            field = value
            notifyChange()
        }
    var muffled: Boolean = false
    var stateBit: Byte = 0b00000001

    // TODO disable calculation of uiProgress then it's not showing on ui
    var progress: Long = 0L
        set(value) {
            field = value
            uiProgress = convertMillisToStringFormatWithSeconds(value)
        }
    var uiProgress: String = convertMillisToStringFormatWithSeconds(progress)
        set(value) {
            field = value
            notifyChange()
        }

    var timePaused: Long = 0L
    var timeRemain: String = convertMillisToStringFormat(duration)

    enum class State {
        WAITING, ACTIVE, COMPLETED, DISABLED
    }

    fun resetStartTime(time: Long = 0L) {
        startTime = time
    }

    fun getNextStartTime(): Long {
        return if (duration == UNCERTAIN || startTime == UNCERTAIN) {
            UNCERTAIN
        }
        else {
            startTime + duration
        }
    }

    fun addPausedOffset(pausedTime: Long) {
        timePaused = pausedTime
    }

    fun continueTask(): Long {
        val currentTime = System.currentTimeMillis()
        val dt = currentTime - lastSystemTime
        progress += dt * 1000
        lastSystemTime = currentTime

        return duration - progress
    }

    fun start() {
        state = State.ACTIVE
    }

    fun disable() {
        when (state) {
            State.COMPLETED, State.ACTIVE -> return
            State.DISABLED -> {
                state = State.WAITING
                duration = originalDuration
            }
            State.WAITING -> {
                state = State.DISABLED
                duration = 0L
            }
        }
    }

    fun stop() {
        state = State.COMPLETED
        duration = progress
    }

    fun complete() {
        state = State.COMPLETED
    }

    fun muffle() {
        if (muffled) {
            vibrationOn = originalVibrationOn
            soundOn = originalSoundOn
            muffled = false
        }
        else {
            vibrationOn = false
            soundOn = false
            muffled = true
        }
    }

    fun notSwappable(): Boolean {
        return ((state == State.COMPLETED) or (state == State.ACTIVE))
    }

    companion object {
        val waiting = 0b00000001
        val active = 0b00000010
        val completed = 0b00000100
        val disabled = 0b00001000
        val muffled = 0b00010000

        var lastSystemTime = 0L

        fun copyFromExistingTask(task: Task): RunningTask {
            task.apply {
                return RunningTask(task.startTime, name, duration, vibrationOn, soundOn, sound)
            }
        }
    }
}