package com.sillyapps.meantime.data

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.sillyapps.meantime.*
import timber.log.Timber

class Task(
    startTime: Long = 0L,

    var name: String = "",
    duration: Long = 0L,
    var vibrationOn: Boolean = true,
    var soundOn: Boolean = true,
    sound: String = AppConstants.DEFAULT_RINGTONE,

    var goalsId: Int = 0
): BaseObservable() {

    @Bindable
    var startTime: Long = startTime
        set(value) {
            field = value
            notifyPropertyChanged(BR.startTime)
        }

    @Bindable
    var duration: Long = duration
        set(value) {
            field = value
            editableDuration = value
            notifyPropertyChanged(BR.duration)
        }

    @Bindable
    var sound: String = sound
        set(value) {
            field = value
            notifyPropertyChanged(BR.sound)
        }

    @Bindable
    var editableDuration: Long = duration
        set(value) {
            field = value
            notifyPropertyChanged(BR.editableDuration)
        }

    @Bindable
    var editableSoundOn = soundOn
        set(value) {
            field = value
            notifyPropertyChanged(BR.editableSoundOn)
        }

    @Bindable
    var editableVibrationOn = vibrationOn
        set(value) {
            field = value
            notifyPropertyChanged(BR.editableVibrationOn)
        }

    @Bindable
    var state: State = State.WAITING
        set(value) {
            field = value
            notifyPropertyChanged(BR.state)
        }

    @Bindable
    var muffled: Boolean = false
        set(value) {
            field = value
            if (!muffled) {
                editableVibrationOn = vibrationOn
                editableSoundOn = soundOn
            }
            else {
                editableVibrationOn = false
                editableSoundOn = false
            }
            notifyPropertyChanged(BR.muffled)
        }

    var stateBit: Byte = 0b00000001

    @Bindable
    var progress: Long = 0L
        set(value) {
            field = value
            notifyPropertyChanged(BR.progress)
        }

    var timePaused: Long = 0L

    fun resetStartTime(time: Long = 0L) {
        startTime = time
    }

    fun getNextStartTime(): Long {
        return if (editableDuration == AppConstants.UNCERTAIN || startTime == AppConstants.UNCERTAIN) {
            AppConstants.UNCERTAIN
        }
        else {
            startTime + editableDuration
        }
    }

    fun addPausedOffset(pausedTime: Long) {
        timePaused = pausedTime
    }

    fun continueTask(): Long {
        val currentTime = System.currentTimeMillis()
        val dt = currentTime - lastSystemTime
        progress += dt
        lastSystemTime = currentTime

        return editableDuration - progress
    }

    fun start() {
        state = State.ACTIVE
        startTime = getLocalCurrentTimeMillis()
    }

    fun disable() {
        when (state) {
            State.COMPLETED, State.ACTIVE -> return
            State.DISABLED -> {
                editableDuration = duration
                state = State.WAITING
            }
            State.WAITING -> {
                editableDuration = 0L
                state = State.DISABLED
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

    fun revertChanges() {
        editableDuration = duration
        editableSoundOn = soundOn
        editableVibrationOn = vibrationOn
        muffled = false
    }

    fun canNotBeSwappedOrDisabled(): Boolean {
        return ((state == State.COMPLETED) or (state == State.ACTIVE))
    }

    fun getProgressInPercents(): String {
        return (progress.toFloat() / editableDuration * 100).toInt().toString() + "%"
    }

    fun isDataValid(): WhatIsWrong {
        if (name == "") return WhatIsWrong.NAME
        if (duration == 0L) return WhatIsWrong.DURATION

        return WhatIsWrong.NOTHING
    }

    fun copy(): Task {
        return Task(startTime, name, duration, vibrationOn, soundOn, sound)
    }

    companion object {
        var lastSystemTime = 0L
    }

    enum class WhatIsWrong {
        NAME, DURATION, NOTHING
    }
}
