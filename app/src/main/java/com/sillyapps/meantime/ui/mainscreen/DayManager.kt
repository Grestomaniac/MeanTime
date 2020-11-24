package com.sillyapps.meantime.ui.mainscreen

import androidx.databinding.Observable
import com.sillyapps.meantime.AppBR
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.BR
import com.sillyapps.meantime.data.Day
import com.sillyapps.meantime.data.State
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.data.repository.AppRepository
import kotlinx.coroutines.*
import timber.log.Timber
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
    private var untilCriticalTimer: Job? = null

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

    suspend fun loadCurrentDay(request: RequestType): Boolean {
        thisDay?.let {
            if (it.isRunning) {
                return false
            }

            it.removeOnPropertyChangedCallback(dataUpdateCallback)
        }

        val day = repository.getDay(request)

        if (day == null) {
            thisDay = null
            return true
        }
        thisDay = day
        thisDay!!.addOnPropertyChangedCallback(dataUpdateCallback)

        return false
    }

    fun start() {
        when (thisDay!!.state) {
            State.WAITING -> startNewDay()
            State.DISABLED -> resumeDay()
            else -> return
        }
    }

    fun pauseDay() {
        if (thisDay!!.state == State.DISABLED) return

        coroutineCounter?.cancel()
        thisDay!!.pause()
    }

    fun getNextTask(stop: Boolean = false) {
        thisDay!!.selectNextTask(stop)
        checkIfCurrentTaskGoalsIsEmpty()
    }

    fun checkIfCurrentTaskGoalsIsEmpty() {
        CoroutineScope(Dispatchers.IO).launch {
            val currentTaskGoals = repository.getTaskGoals(thisDay!!.currentTask.goalsId)
            currentTaskGoalsIsNotEmpty = currentTaskGoals.goals.isNotEmpty()
        }
    }

    fun recalculateStartTimes(position: Int) {
        thisDay!!.recalculateStartTimes(position)
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
        thisDay!!.stopDayManually()
    }

    private fun cancelTimers() {
        coroutineCounter?.cancel()
        untilCriticalTimer?.cancel()
    }

    fun screenIsOff() {
        if (thisDay!!.timeRemain < AppConstants.CRITICAL_TIME_REMAINED) {
            return
        }
        else {
            tickInterval = AppConstants.BATTERY_SAVING_INTERVAL
            startUntilCriticalTimer()
        }
    }

    fun screenIsOn() {
        tickInterval = AppConstants.NORMAL_INTERVAL
        untilCriticalTimer?.cancel()
        coroutineCounter?.cancel()
        startCoroutineCounter()
    }

    private fun startCoroutineCounter() {
        Task.lastSystemTime = System.currentTimeMillis()
        coroutineCounter = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                val timeRemained = thisDay!!.currentTask.continueTask()
                if (timeRemained < 0) {
                    getNextTask()
                }
                else {
                    thisDay!!.updateTimeRemained(timeRemained)
                }

                delay(tickInterval)
            }
        }
    }

    private fun startUntilCriticalTimer() {
        untilCriticalTimer = CoroutineScope(Dispatchers.Default).launch {
            delay(thisDay!!.timeRemain - AppConstants.CRITICAL_TIME_REMAINED)
            tickInterval = AppConstants.NORMAL_INTERVAL
        }
    }

}