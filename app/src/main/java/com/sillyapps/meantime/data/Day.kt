package com.sillyapps.meantime.data

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.sillyapps.meantime.AppBR
import com.sillyapps.meantime.BR
import com.sillyapps.meantime.getLocalCurrentTimeMillis
import java.util.*

class Day(val tasks: MutableList<Task>,
          val alarmDuration: Long,
          var state: DayState = DayState.WAITING,
          var dayStartTime: Long = 0L,
          currentTaskPos: Int = 0,
          ): BaseObservable() {

    @Bindable("currentTaskPos")
    var currentTaskPos: Int = currentTaskPos
        set(value) {
            field = value
            currentTask = tasks[value]
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

    @Bindable
    var currentTask: Task = tasks[currentTaskPos]

    fun start() {
        currentTaskPos = 0
        dayStartTime = getLocalCurrentTimeMillis()
        state = DayState.ACTIVE
        resetTasks()
        currentTask.start()
        timeRemain = currentTask.editableDuration
        runningState = true
    }

    fun selectNextTask(stop: Boolean = false): Boolean {
        if (atLastTask()) {
            return false
        }
        completeCurrentTask(stop)

        currentTaskPos++
        currentTask.let {
            if (it.state == Task.State.DISABLED) {
                selectNextTask(true)
                return true
            }
            it.start()
            recalculateStartTimes(currentTaskPos+1)
        }
        return true
    }

    fun endDay(stop: Boolean) {
        if (!stop) notifyPropertyChanged(AppBR.taskFinishedNaturally)

        dayStartTime = 0L
        state = DayState.COMPLETED
        for (i in currentTaskPos until tasks.size)
            tasks[i].complete()
        timeRemain = 0L
        runningState = false
    }

    fun pause() {
        state = DayState.PAUSED
    }

    fun resume() {
        state = DayState.ACTIVE
        val timePaused = System.currentTimeMillis() - Task.lastSystemTime
        currentTask.addPausedOffset(timePaused)
    }

    fun getPreviousTask(): Task {
        return tasks[currentTaskPos-1]
    }

    fun updateTimeRemained(newTime: Long = currentTask.editableDuration) {
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
            currentTask.stop()
            updateTasks(currentTaskPos)
            timeRemain = currentTask.editableDuration
        }
        else {
            currentTask.complete()
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
                return Day(template.activities, currentTemplate.alarmDuration)
            }
            return null
        }
    }
}