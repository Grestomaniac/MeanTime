package com.sillyapps.meantime.data

import androidx.databinding.BaseObservable
import com.sillyapps.meantime.getLocalCurrentTimeMillis

class Day(var tasks: MutableList<RunningTask>,
          var state: DayState = DayState.WAITING,
          var dayStartTime: Long = 0L,
          currentTaskPos: Int = 0,
          var previousTaskPos: Int = -1): BaseObservable() {

    var currentTaskPos: Int = currentTaskPos
        set(value) {
            previousTaskPos = currentTaskPos
            field = value
            notifyChange()
        }

    fun start() {
        dayStartTime = getLocalCurrentTimeMillis()
        state = DayState.ACTIVE
        resetTasks()
        tasks[currentTaskPos].start()

        notifyChange()
    }

    fun selectNextTask() {
        if (currentTaskPos == tasks.lastIndex) {
            endDay()
            return
        }
        currentTaskPos++
        getCurrentTask().start()
    }

    fun stop() {
        getCurrentTask().stop()
        updateTasks(currentTaskPos)
        selectNextTask()
    }

    fun endDay() {
        dayStartTime = 0L
        state = DayState.COMPLETED
        getCurrentTask().complete()
        resetTasks()

        notifyChange()
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

    private fun resetTasks() {
        tasks[0].startTime = dayStartTime
        for (i in 1 until tasks.size) {
            tasks[i].startTime = tasks[i-1].getNextStartTime()
        }
    }

    private fun updateTasks(from: Int) {
        for (i in from+1 until tasks.size) {
            tasks[i].startTime = tasks[i-1].getNextStartTime()
        }
    }

    enum class DayState {
        WAITING, PAUSED, COMPLETED, ACTIVE
    }

    companion object {
        fun fromTemplate(template: Template?): Day? {
            template?.let { currentTemplate ->
                val runningTasks = currentTemplate.activities.map { RunningTask.copyFromExistingTask(it) }.toMutableList()
                return Day(runningTasks)
            }
            return null
        }
    }
}