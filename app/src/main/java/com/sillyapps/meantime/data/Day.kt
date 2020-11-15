package com.sillyapps.meantime.data

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.sillyapps.meantime.AppBR
import com.sillyapps.meantime.BR
import com.sillyapps.meantime.getLocalCurrentTimeMillis
import java.util.*

class Day(val tasks: MutableList<Task>,
          val alarmDuration: Long,
          var state: State = State.WAITING,
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

    var completedTaskPos: Int = -1

    fun start() {
        currentTaskPos = 0
        dayStartTime = getLocalCurrentTimeMillis()
        state = State.ACTIVE
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
            if (it.state == State.DISABLED) {
                return selectNextTask(true)
            }
            it.start()
            recalculateStartTimes(currentTaskPos+1)
        }
        return true
    }

    fun endDay(stop: Boolean) {
        if (!stop) {
            completedTaskPos = currentTaskPos
            notifyPropertyChanged(AppBR.taskFinishedNaturally)
        }

        dayStartTime = 0L
        state = State.COMPLETED
        for (i in currentTaskPos until tasks.size)
            tasks[i].complete()
        timeRemain = 0L
        runningState = false
    }

    fun pause() {
        state = State.DISABLED
    }

    fun resume() {
        state = State.ACTIVE
        val timePaused = System.currentTimeMillis() - Task.lastSystemTime
        currentTask.addPausedOffset(timePaused)
    }

    fun getCompletedTask(offset: Int = 0): Task {
        return tasks[completedTaskPos + offset]
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
            completedTaskPos = currentTaskPos
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

    companion object {
        fun fromTemplate(template: Template?): Day? {
            template?.let {
                return Day(it.activities, it.alarmDuration)
            }
            return null
        }
    }
}