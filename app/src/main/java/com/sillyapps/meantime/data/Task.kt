package com.sillyapps.meantime.data

import android.util.Log
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.sillyapps.meantime.*
import com.sillyapps.meantime.utils.getLocalCurrentTimeMillis
import timber.log.Timber

class Task(
    startTime: Long = 0L,

    var name: String = "",
    duration: Long = 0L,
    var vibrationOn: Boolean = true,
    var soundOn: Boolean = true,
    sound: String = AppConstants.DEFAULT_RINGTONE,
    var goalsId: Int = 0,
    val temporal: Boolean = false,
    uncertain: Boolean = false
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

    @Bindable
    var progress: Long = 0L
        set(value) {
            field = value
            taskTimeRemained = editableDuration - progress
            notifyPropertyChanged(BR.progress)
        }

    @Bindable
    var taskTimeRemained: Long = editableDuration
        set(value) {
            field = value
            notifyPropertyChanged(BR.taskTimeRemained)
        }

    @Bindable
    var relativeProgress: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.relativeProgress)
        }

    @Bindable
    var uncertain: Boolean = uncertain
        set(value) {
            field = value
            notifyPropertyChanged(BR.uncertain)
        }

    fun resetStartTime(time: Long = 0L) {
        startTime = time
    }

    fun getNextStartTime(): Long {
        return if (uncertain || startTime == AppConstants.UNCERTAIN) {
            AppConstants.UNCERTAIN
        }
        else {
            startTime + editableDuration - progress
        }
    }

    fun continueTask(): Long {
        val currentTime = System.currentTimeMillis()
        val dt = currentTime - lastSystemTime
        progress += dt
        lastSystemTime = currentTime

        return taskTimeRemained
    }

    fun updateRelativeProgress() {
        relativeProgress = (progress.toFloat() / editableDuration * 100).toInt()
    }

    fun start() {
        state = State.ACTIVE
        startTime = getLocalCurrentTimeMillis()
    }

    fun pause() {
        state = State.WAITING
        lastSystemTime = System.currentTimeMillis()
    }

    fun resume() {
        state = State.ACTIVE
        lastSystemTime = System.currentTimeMillis()
    }

    fun disable() {
        when (state) {
            State.DISABLED -> {
                editableDuration = duration
                state = State.WAITING
            }
            State.WAITING -> {
                editableDuration = 0L
                state = State.DISABLED
            }
            else -> return
        }
    }

    fun stop() {
        state = State.COMPLETED
        duration = progress
        relativeProgress = 100
        taskTimeRemained = 0L
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
        return (progress.toFloat() / editableDuration).toInt().toString() + "%"
    }

    fun isDataValid(): WhatIsWrong {
        if (name == "") return WhatIsWrong.NAME
        if ((editableDuration == 0L) and (!uncertain)) return WhatIsWrong.DURATION

        return WhatIsWrong.NOTHING
    }

    fun copyDataFrom(task: Task) {
        editableDuration = task.editableDuration
        uncertain = task.uncertain
        editableVibrationOn = task.editableVibrationOn
        editableSoundOn = task.editableSoundOn
    }

    fun copy(): Task {
        return Task(startTime, name, editableDuration, editableVibrationOn, editableSoundOn, uncertain=uncertain)
    }

    companion object {
        var lastSystemTime = 0L
    }

    enum class WhatIsWrong {
        NAME, DURATION, NOTHING
    }
}
