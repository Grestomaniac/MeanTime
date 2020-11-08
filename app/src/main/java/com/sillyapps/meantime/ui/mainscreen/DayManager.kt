package com.sillyapps.meantime.ui.mainscreen

import android.os.CountDownTimer
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.data.Day
import com.sillyapps.meantime.data.RunningTask
import com.sillyapps.meantime.data.repository.AppRepository
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DayManager @Inject constructor(private val repository: AppRepository) {

    var thisDay: Day? = null

    suspend fun loadCurrentDay(): Boolean {
        thisDay?.let {
            if (it.runningState) {
                return false
            }
        }

        repository.setNewDay()
        val day = repository.getCurrentDay()
            ?: //No templates found
            return true
        thisDay = day

        return false
    }

    fun start() {
        when (thisDay!!.state) {
            Day.DayState.WAITING -> startNewDay()
            Day.DayState.PAUSED -> resumeDay()
            else -> return
        }
    }

    fun pauseDay() {
        timer.cancel()
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

    private fun startTimer() {
        RunningTask.lastSystemTime = System.currentTimeMillis()
        timer.start()
    }

    private fun startNewDay() {
        thisDay!!.start()
        startTimer()
    }

    private fun resumeDay() {
        thisDay!!.resume()
        startTimer()
    }

    private fun resetDay(stop: Boolean) {
        timer.cancel()
        thisDay!!.endDay(stop)
        return
    }

    private val timer = object : CountDownTimer(AppConstants.TIMER_CHECK_INTERVAL, 1000L) {

        override fun onTick(millisUntilFinished: Long) {
            val timeRemained = thisDay!!.getCurrentTask().continueTask()
            if (timeRemained < 0) {
                getNextTask()
            } else
                thisDay!!.updateTimeRemained(timeRemained)
        }

        override fun onFinish() {
            start()
        }
    }

}