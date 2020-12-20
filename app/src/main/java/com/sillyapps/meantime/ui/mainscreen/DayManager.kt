package com.sillyapps.meantime.ui.mainscreen

import androidx.databinding.Observable
import androidx.lifecycle.LiveData
import com.sillyapps.meantime.AppBR
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.data.Day
import com.sillyapps.meantime.data.State
import com.sillyapps.meantime.data.TaskGoals
import com.sillyapps.meantime.data.repository.AppRepository
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DayManager @Inject constructor(private val repository: AppRepository) {

    enum class RequestType {
        GET_NEXT, REFRESH, GET_CURRENT
    }

    var thisDay: Day? = null

    private var tickInterval = AppConstants.NORMAL_INTERVAL
    private var coroutineCounter: Job? = null
    /*private var untilCriticalTimer: Job? = null*/

    var currentTaskGoalsIsNotEmpty: Boolean = false

    private val dataUpdateCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            when (propertyId) {

                AppBR.dayEnded -> {
                    cancelTimers()
                }

            }
        }
    }

    suspend fun loadCurrentDay(request: RequestType) {
        thisDay?.let {
            if (it.isRunning) {
                return
            }
            it.removeOnPropertyChangedCallback(dataUpdateCallback)
        }

        thisDay = repository.getDay(request) ?: Day()
        thisDay!!.addOnPropertyChangedCallback(dataUpdateCallback)
    }

    fun start() {
        if (thisDay!!.tasks.isEmpty()) return
        when (thisDay!!.dayState) {
            State.WAITING -> startNewDay()
            State.DISABLED -> resumeDay()
            else -> return
        }
    }

    fun stopTask() {
        when (thisDay!!.dayState) {
            State.ACTIVE, State.DISABLED -> getNextTask(true)
            else -> return
        }
    }

    fun pauseDay() {
        coroutineCounter?.cancel()
        thisDay!!.pause()
        CoroutineScope(Dispatchers.IO).launch { repository.saveDay(thisDay) }
    }

    private fun getNextTask(stop: Boolean = false) {
        thisDay!!.selectNextTask(stop)
        checkIfCurrentTaskGoalsIsEmpty()
    }

    private fun checkIfCurrentTaskGoalsIsEmpty() {
        CoroutineScope(Dispatchers.IO).launch {
            val currentTaskGoals = repository.getTaskGoals(thisDay!!.currentTask.goalsId)
            currentTaskGoals?.let { currentTaskGoalsIsNotEmpty = it.activeGoals.isNotEmpty() }
        }
    }

    fun observeTaskGoals(): LiveData<List<TaskGoals>> {
        return repository.observeAllTaskGoals()
    }

    fun recalculateStartTimes(position: Int) {
        thisDay!!.taskDropped(position)
    }

    suspend fun getTaskGoalsIdByName(taskName: String): Int {
        return repository.getTaskGoalIdByName(taskName)
    }

    fun notifyTasksSwapped(upperPosition: Int, bottomPosition: Int) {
        thisDay!!.notifyTasksSwapped(upperPosition, bottomPosition)
    }

    fun notifyTaskDisabled(position: Int) {
        thisDay!!.notifyTaskDisabled(position)
    }

    private fun startNewDay() {
        thisDay!!.start()
        startCoroutineCounter()
        checkIfCurrentTaskGoalsIsEmpty()
    }

    private fun resumeDay() {
        thisDay!!.resume()
        startCoroutineCounter()
    }

    fun resetDay() {
        when (thisDay!!.dayState) {
            State.ACTIVE, State.DISABLED -> thisDay!!.stopDayManually()
            else -> return
        }
    }

    private fun cancelTimers() {
        coroutineCounter?.cancel()
        /*untilCriticalTimer?.cancel()*/
    }

    fun screenIsOff() {
        /*if (thisDay!!.timeRemain < AppConstants.CRITICAL_TIME_REMAINED) {
            return
        }
        else {
            tickInterval = AppConstants.BATTERY_SAVING_INTERVAL
        }*/
    }

    fun screenIsOn() {
        /*tickInterval = AppConstants.NORMAL_INTERVAL
        untilCriticalTimer?.cancel()
        coroutineCounter?.cancel()
        startCoroutineCounter()*/
    }

    private fun startCoroutineCounter() {
        coroutineCounter = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                tick()

                delay(tickInterval)
            }
        }
    }

    private fun tick() {
        val timeRemained = thisDay!!.currentTask.continueTask()
        if (timeRemained < 0) {
            getNextTask()
        }
        else {
            thisDay!!.updateTimeRemained(timeRemained)
        }
    }

}