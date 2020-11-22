package com.sillyapps.meantime.data

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.sillyapps.meantime.AppBR
import com.sillyapps.meantime.BR
import com.sillyapps.meantime.getLocalCurrentTimeMillis
import java.util.*

class Day(val tasks: MutableList<Task>,
          val alarmDuration: Long,
          val templateId: Int = 0,
          var state: State = State.WAITING,
          var dayStartTime: Long = 0L,
          currentTaskPos: Int = 0
          ): BaseObservable() {

    @Bindable
    var currentTaskPos: Int = currentTaskPos
        set(value) {
            field = value
            currentTask = tasks[value]
        }

    @Bindable
    var timeRemain: Long = 0L
        set(value) {
            field = value
            notifyPropertyChanged(BR.timeRemain)
        }

    @Bindable
    var isRunning: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.isRunning)
        }

    @Bindable
    var currentTask: Task = tasks[currentTaskPos]

    var completedTaskPos: Int = -1

    fun start() {
        currentTaskPos = 0
        dayStartTime = getLocalCurrentTimeMillis()
        state = State.ACTIVE
        resetTasks()
        startCurrentTask()
        timeRemain = currentTask.editableDuration
        isRunning = true
    }

    private fun startCurrentTask() {
        if (currentTask.state == State.DISABLED) {
            selectNextTask(true)
        }
        else {
            currentTask.start()
            recalculateStartTimes(currentTaskPos+1)
        }
    }

    fun selectNextTask(stop: Boolean = false) {
        completeCurrentTask(stop)

        if (atLastTask()) {
            endDay()
            return
        }

        currentTaskPos++
        startCurrentTask()
    }

    private fun endDay() {
        dayStartTime = 0L
        state = State.COMPLETED
        timeRemain = 0L
        isRunning = false
        notifyPropertyChanged(AppBR.dayEnded)
    }

    fun stopDayManually() {
        for (i in currentTaskPos until tasks.size)
            tasks[i].complete()
        endDay()
    }


    fun pause() {
        state = State.DISABLED
        notifyPropertyChanged(AppBR.dayPausedOrUnPaused)
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

    private fun atLastTask(): Boolean {
        return currentTaskPos == tasks.lastIndex
    }

    companion object {
        fun fromTemplate(template: Template?): Day? {
            template?.let {
                return Day(it.activities, it.alarmDuration, it.id)
            }
            return null
        }
    }
}