package com.sillyapps.meantime.data

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.sillyapps.meantime.*

class Task(
    startTime: Long = 0L,

    var name: String = "",
    duration: Long = 0L,
    var vibrationOn: Boolean = true,
    var soundOn: Boolean = true,
    sound: String = AppConstants.DEFAULT_RINGTONE
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
            notifyPropertyChanged(BR.duration)
        }

    @Bindable
    var sound: String = sound
        set(value) {
            field = value
            notifyPropertyChanged(BR.sound)
        }

    fun getNextStartTime(): Long {
        return if (duration == AppConstants.UNCERTAIN || startTime == AppConstants.UNCERTAIN) {
            AppConstants.UNCERTAIN
        }
        else {
            startTime + duration
        }
    }

    fun isDataValid(): WhatIsWrong {
        if (name == "") return WhatIsWrong.NAME
        if (duration == 0L) return WhatIsWrong.DURATION

        return WhatIsWrong.NOTHING
    }

    enum class WhatIsWrong {
        NAME, DURATION, NOTHING
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
            notifyChange()
        }

    var duration: Long = originalDuration
        set(value) {
            field = value
        }

    var soundOn = originalSoundOn
    var vibrationOn = originalVibrationOn

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
            notifyPropertyChanged(BR.`muffled$1`)
        }

    var stateBit: Byte = 0b00000001

    var progress: Long = 0L
        set(value) {
            field = value
            notifyChange()
        }

    var timePaused: Long = 0L

    enum class State {
        WAITING, ACTIVE, COMPLETED, DISABLED
    }

    fun resetStartTime(time: Long = 0L) {
        startTime = time
    }

    fun getNextStartTime(): Long {
        return if (duration == AppConstants.UNCERTAIN || startTime == AppConstants.UNCERTAIN) {
            AppConstants.UNCERTAIN
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
        progress += dt
        lastSystemTime = currentTime

        return duration - progress
    }

    fun start() {
        state = State.ACTIVE
        startTime = getLocalCurrentTimeMillis()
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

    fun getProgressInPercents(): String {
        return (progress.toFloat() / duration * 100).toInt().toString() + "%"
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