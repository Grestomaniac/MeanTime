package com.sillyapps.meantime.ui.mainscreen

import androidx.databinding.BaseObservable
import androidx.lifecycle.viewModelScope
import com.sillyapps.meantime.data.Day
import com.sillyapps.meantime.data.PropertyAwareMutableLiveData
import com.sillyapps.meantime.data.RunningTask
import com.sillyapps.meantime.data.repository.AppRepository
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DayManager @Inject constructor(private val repository: AppRepository) {

    var thisDay: Day? = null

    private var timerJob: Job? = null

    suspend fun loadCurrentDay(): Boolean {
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
        timerJob?.cancel()
        thisDay!!.pause()
    }

    fun getNextTask() {
        thisDay!!.let {
            if (it.atLastTask()) {
                resetDay()
                return
            }

            it.selectNextTask(true)
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
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            RunningTask.lastSystemTime = System.currentTimeMillis()

            while (true) {
                val timeRemained = thisDay!!.getCurrentTask().continueTask()
                if (timeRemained < 0) {
                    getNextTask()
                }
                else
                    thisDay!!.updateTimeRemained(timeRemained)
                delay(1000)
            }
        }
    }

    private fun startNewDay() {
        thisDay!!.start()
        startTimer()
    }

    private fun resumeDay() {
        thisDay!!.resume()
        startTimer()
    }

    private fun resetDay() {
        timerJob?.cancel()
        thisDay!!.endDay()
        return
    }

}