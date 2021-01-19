package com.sillyapps.meantime.data

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.sillyapps.meantime.*
import com.sillyapps.meantime.utils.getLocalCurrentTimeMillis
import java.util.*

class Day(val tasks: MutableList<Task> = mutableListOf(),
          val templateId: Int = 0,
          dayState: State = State.WAITING,
          var dayStartTime: Long = 0L,
          currentTaskPos: Int = AppConstants.NOT_ASSIGNED
          ): BaseObservable() {

    @Bindable
    var dayState: State = dayState
        set(value) {
            field = value
            notifyPropertyChanged(BR.dayState)
        }

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

    var timePaused = 0L
    var isCurrentTaskReplacedWhilePaused: Boolean = false

    var currentTask: Task = Task()

    var completedTaskPos: Int = -1

    fun start() {
        currentTaskPos = 0
        dayStartTime = getLocalCurrentTimeMillis()
        dayState = State.ACTIVE
        resetTasks()
        Task.lastSystemTime = System.currentTimeMillis()

        startCurrentTask()
        isRunning = true
    }

    private fun startCurrentTask() {
        if (currentTask.state == State.DISABLED) {
            selectNextTask(true)
        }
        else {
            currentTask.start()
            notifyPropertyChanged(AppBR.currentTaskStateChanged)
            updateStartTimes(currentTaskPos+1)
        }
    }

    fun selectNextTask(stop: Boolean = false) {
        completeCurrentTask(stop)
        timePaused = 0L

        if (atLastTask()) {
            endDay()
            return
        }

        currentTaskPos++
        startCurrentTask()
    }

    private fun completeCurrentTask(stop: Boolean) {
        if (stop) {
            currentTask.stop()
            updateStartTimes(currentTaskPos+1)
        }
        else {
            currentTask.complete()
            completedTaskPos = currentTaskPos
            notifyPropertyChanged(AppBR.taskFinishedNaturally)
        }
        notifyPropertyChanged(AppBR.currentTaskStateChanged)
    }

    private fun endDay() {
        dayStartTime = 0L
        dayState = State.COMPLETED
        timeRemain = 0L
        isRunning = false
        notifyPropertyChanged(AppBR.dayEnded)
    }

    fun stopDayManually() {
        for (i in currentTaskPos until tasks.size)
            tasks[i].complete()
        endDay()
    }

    fun addTemporalTask(task: Task) {
        if (tasks.isNotEmpty()) {
            task.startTime = tasks.last().getNextStartTime()
        }
        tasks.add(task)
        notifyPropertyChanged(AppBR.taskAdded)
    }

    fun pause() {
        dayState = State.DISABLED
        currentTask.pause()

        notifyPropertyChanged(AppBR.dayPaused)
        notifyPropertyChanged(AppBR.currentTaskStateChanged)
    }

    fun resume() {
        dayState = State.ACTIVE
        timePaused += (System.currentTimeMillis() - Task.lastSystemTime)

        if (currentTask.state == State.DISABLED) {
            selectNextTask(true)
            return
        }
        resumeCurrentTask()

        recalculateStartTimes(currentTaskPos)
    }

    private fun resumeCurrentTask() {
        currentTask = tasks[currentTaskPos]
        currentTask.resume()
        notifyPropertyChanged(AppBR.currentTaskStateChanged)
    }

    fun getCompletedTask(offset: Int = 0): Task {
        return tasks[completedTaskPos + offset]
    }

    fun updateTimeRemained(newTime: Long = currentTask.editableDuration) {
        timeRemain = newTime
    }

    private fun resetTasks() {
        tasks[0].startTime = dayStartTime
        updateStartTimes(1)
    }

    private fun recalculateStartTimes(position: Int) {
        if (position == 0) {
            tasks[position].startTime = getLocalCurrentTimeMillis()
            updateStartTimes(position+1)
            return
        }
        else if (position == currentTaskPos) {
            tasks[position].startTime = getLocalCurrentTimeMillis()
            updateStartTimes(position+1)
            return
        }
        updateStartTimes(position)
    }

    private fun updateStartTimes(position: Int) {
        for (i in position until tasks.size) {
            tasks[i].resetStartTime(tasks[i-1].getNextStartTime())
        }
    }

    fun taskDropped(position: Int) {
        if (position == currentTaskPos) {
            isCurrentTaskReplacedWhilePaused = true
        }
        recalculateStartTimes(position)
    }

    fun notifyTaskDisabled(position: Int) {
        tasks[position].disable()
        recalculateStartTimes(position)
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
                return Day(it.activities, it.id)
            }
            return null
        }
    }
}