package com.sillyapps.meantime.ui.mainscreen

import com.sillyapps.meantime.AppConstants
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

    suspend fun loadCurrentDay(request: RequestType): Boolean {
        thisDay?.let {
            if (it.isRunning) {
                return false
            }
        }

        val day = repository.getDay(request)

        if (day == null) {
            thisDay = null
            return true
        }
        Timber.d("setting new day")
        thisDay = day

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
        coroutineCounter?.cancel()
        thisDay!!.pause()
    }

    fun getNextTask(stop: Boolean = false) {
        val dayNotEnded = thisDay!!.selectNextTask(stop)
        if (!dayNotEnded) {
            resetDay(stop)
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

    /*private fun startTimer() {
        Task.lastSystemTime = System.currentTimeMillis()
        timer.start()
    }*/

    private fun startNewDay() {
        thisDay!!.start()
        /*startTimer()*/
        startCoroutineCounter()
    }

    private fun resumeDay() {
        thisDay!!.resume()
        startCoroutineCounter()
    }

    fun resetDay(stop: Boolean) {
        /*timer.cancel()*/
        coroutineCounter?.cancel()
        untilCriticalTimer?.cancel()
        thisDay!!.endDay(stop)
        return
    }

    fun screenIsOff() {
        if (thisDay!!.timeRemain < AppConstants.CRITICAL_TIME_REMAINED) {
            Timber.d("No time left, returning")
            return
        }
        else {
            Timber.d("Setting battery saving interval")
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
            Timber.d("Coroutine launched")
            while (true) {
                val timeRemained = thisDay!!.currentTask.continueTask()
                if (timeRemained < 0) {
                    getNextTask()
                }

                thisDay!!.updateTimeRemained(timeRemained)
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

    /*private val timer = object : CountDownTimer(AppConstants.TIMER_CHECK_INTERVAL, 1000L) {

        override fun onTick(millisUntilFinished: Long) {
            val timeRemained = thisDay!!.currentTask.continueTask()
            if (timeRemained < 0) {
                getNextTask()
            } else
                thisDay!!.updateTimeRemained(timeRemained)
        }

        override fun onFinish() {
            start()
        }
    }*/
}