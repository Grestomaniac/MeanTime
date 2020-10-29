package com.sillyapps.meantime.data

import androidx.databinding.BaseObservable
import com.sillyapps.meantime.getLocalCurrentTimeMillis
import java.util.*

class Day(var tasks: MutableList<RunningTask>,
          var state: DayState = DayState.WAITING,
          var dayStartTime: Long = 0L,
          currentTaskPos: Int = 0,
          ): BaseObservable() {

    var currentTaskPos: Int = currentTaskPos
        set(value) {
            field = value
            notifyChange()
        }

    var timeRemain: Long = 0L
        set(value) {
            field = value
            notifyChange()
        }

    var running: Boolean = false
        set(value) {
            field = value
            notifyChange()
        }

    fun start() {
        dayStartTime = getLocalCurrentTimeMillis()
        state = DayState.ACTIVE
        resetTasks()
        tasks[currentTaskPos].start()
        timeRemain = getCurrentTask().duration

        notifyChange()
    }

    fun selectNextTask(stop: Boolean = false) {
        if (stop) {
            getCurrentTask().stop()
            updateTasks(currentTaskPos)
            timeRemain = getCurrentTask().duration
        }
        else getCurrentTask().complete()

        currentTaskPos++
        getCurrentTask().let {
            it.start()
            recalculateStartTimes(currentTaskPos+1)
        }
    }

    fun endDay() {
        dayStartTime = 0L
        state = DayState.COMPLETED
        getCurrentTask().complete()
        resetTasks()
        timeRemain = 0L

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

    fun updateTimeRemained(newTime: Long = getCurrentTask().duration) {
        timeRemain = newTime
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
                return Day(runningTasks)
            }
            return null
        }
    }
}