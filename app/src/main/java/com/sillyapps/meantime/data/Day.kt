package com.sillyapps.meantime.data

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.sillyapps.meantime.AppBR
import com.sillyapps.meantime.BR
import com.sillyapps.meantime.getLocalCurrentTimeMillis
import timber.log.Timber
import java.util.*

class Day(val tasks: MutableList<RunningTask>,
          val alarmDuration: Long,
          var state: DayState = DayState.WAITING,
          var dayStartTime: Long = 0L,
          currentTaskPos: Int = 0,
          ): BaseObservable() {

    @Bindable("currentTaskPos")
    var currentTaskPos: Int = currentTaskPos
        set(value) {
            field = value
        }

    @Bindable("timeRemained")
    var timeRemain: Long = 0L
        set(value) {
            field = value
            notifyPropertyChanged(BR.timeRemain)
        }

    @Bindable("runningState")
    var runningState: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.runningState)
        }

    fun start() {
        dayStartTime = getLocalCurrentTimeMillis()
        state = DayState.ACTIVE
        resetTasks()
        tasks[currentTaskPos].start()
        timeRemain = getCurrentTask().duration
        runningState = true
    }

    fun selectNextTask(stop: Boolean = false): Boolean {
        if (atLastTask()) {
            return false
        }
        completeCurrentTask(stop)

        currentTaskPos++
        getCurrentTask().let {
            if (it.state == RunningTask.State.DISABLED) selectNextTask(true)
            it.start()
            recalculateStartTimes(currentTaskPos+1)
        }
        return true
    }

    fun endDay(stop: Boolean) {
        if (!stop) notifyPropertyChanged(AppBR.taskFinishedNaturally)

        dayStartTime = 0L
        state = DayState.COMPLETED
        getCurrentTask().complete()
        resetTasks()
        timeRemain = 0L
        runningState = false
    }

    fun pause() {
        state = DayState.PAUSED
    }

    fun resume() {
        state = DayState.ACTIVE
        val timePaused = System.currentTimeMillis() - RunningTask.lastSystemTime
        getCurrentTask().addPausedOffset(timePaused)
    }

    fun getCurrentTask(): RunningTask {
        return tasks[currentTaskPos]
    }

    fun getPreviousTask(): RunningTask {
        return tasks[currentTaskPos-1]
    }

    fun updateTimeRemained(newTime: Long = getCurrentTask().duration) {
        timeRemain = newTime
    }

    private fun resetTasks() {
        tasks[0].startTime = dayStartTime
        for (i in 1 until tasks.size) {
            tasks[i].startTime = tasks[i-1].getNextStartTime()
        }
    }

    private fun completeCurrentTask(stop: Boolean) {
        if (stop) {
            getCurrentTask().stop()
            updateTasks(currentTaskPos)
            timeRemain = getCurrentTask().duration
        }
        else {
            getCurrentTask().complete()
            notifyPropertyChanged(AppBR.taskFinishedNaturally)
        }
    }

    private fun updateTasks(from: Int) {
        for (i in from+1 until tasks.size) {
            tasks[i].startTime = tasks[i-1].getNextStartTime()
        }
    }

    fun recalculateStartTimes(position: Int) {
        var pos = position
        if (pos == 0) {
            tasks[pos].resetStartTime()
            pos++
        }
        for (i in pos until tasks.size) {
            tasks[i].resetStartTime(tasks[i-1].getNextStartTime())
        }
    }

    fun notifyTaskDisabled(position: Int) {
        tasks[position].disable()
        recalculateStartTimes(position)
        notifyChange()
    }

    fun notifyTasksSwapped(upperPosition: Int, bottomPosition: Int) {
        Collections.swap(tasks, upperPosition, bottomPosition)
    }

    fun atLastTask(): Boolean {
        return currentTaskPos == tasks.lastIndex
    }

    enum class DayState {
        WAITING, PAUSED, COMPLETED, ACTIVE
    }

    companion object {
        fun fromTemplate(template: Template?): Day? {
            template?.let { currentTemplate ->
                val runningTasks = currentTemplate.activities.map { RunningTask.copyFromExistingTask(it) }.toMutableList()
                return Day(runningTasks, currentTemplate.alarmDuration)
            }
            return null
        }
    }
}