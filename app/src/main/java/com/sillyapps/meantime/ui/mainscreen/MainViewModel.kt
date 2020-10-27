package com.sillyapps.meantime.ui.mainscreen

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.sillyapps.meantime.convertMillisToStringFormatWithSeconds
import com.sillyapps.meantime.data.Day
import com.sillyapps.meantime.data.PropertyAwareMutableLiveData
import com.sillyapps.meantime.data.RunningTask
import com.sillyapps.meantime.data.repository.AppRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel @ViewModelInject constructor(private val repository: AppRepository): ViewModel() {

    val currentDay: PropertyAwareMutableLiveData<Day> = PropertyAwareMutableLiveData()
    val tasks: LiveData<MutableList<RunningTask>> = currentDay.map { it.tasks }
    val currentTask: LiveData<RunningTask> = currentDay.map { currentTaskPos = it.currentTaskPos
                                                              it.tasks[it.currentTaskPos] }
    var currentTaskPos: Int = 0

    private val timeRemain: MutableLiveData<Long> = MutableLiveData(0)
    val uiTimeRemain: LiveData<String> = timeRemain.map {
        convertMillisToStringFormatWithSeconds(timeRemain.value!!) }

    private val _noTemplate: MutableLiveData<Boolean> = MutableLiveData(false)
    val noTemplate: LiveData<Boolean> = _noTemplate

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val day = repository.getCurrentDay()
            currentDay.postValue(day)

            if (day == null) {
                //No templates show navigate to templateEditor
                _noTemplate.value = true
            }
        }
    }

    fun startButtonPressed() {
        currentDay.value?.let {
            when (it.state) {
                Day.DayState.WAITING -> startNewDay()
                Day.DayState.PAUSED -> resumeDay()
                else -> return
            }
        }
    }

    private fun startNewDay() {
        currentDay.value!!.start()
        timeRemain.value = currentTask.value!!.duration
        startTimer()
    }

    private fun resumeDay() {
        currentDay.value!!.resume()
        startTimer()
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            RunningTask.lastSystemTime = System.currentTimeMillis()

            while (true) {
                val timeRemained = currentTask.value!!.continueTask()
                if (timeRemained < 0) {
                    currentTask.value!!.complete()
                    getNextTask()
                }

                updateTimeRemained(timeRemained)
                delay(1000)
            }
        }
    }

    private fun getNextTask() {
        currentDay.value!!.apply {
            selectNextTask()
            updateTimeRemained(currentTask.value!!.duration)
        }
    }

    fun pauseDay() {
        timerJob?.cancel()
        currentDay.value!!.pause()
    }

    fun stop() {
        if (currentTaskPos == tasks.value!!.lastIndex) {
            timerJob?.cancel()
            updateTimeRemained(0)
            currentDay.value!!.endDay()
            return
        }
        currentDay.value!!.stop()

        updateTimeRemained(currentTask.value!!.duration)
    }

    private fun updateTimeRemained(time: Long) {
        timeRemain.value = time
    }

    fun recalculateStartTimes(position: Int) {
        tasks.value?.let {
            var pos = position
            if (pos == 0) {
                it[pos].resetStartTime()
                pos++
            }
            for (i in pos until it.size) {
                it[i].resetStartTime(it[i-1].getNextStartTime())
            }
        }

    }

    fun notifyTasksSwapped(upperPosition: Int, bottomPosition: Int) {
        Collections.swap(tasks.value!!, upperPosition, bottomPosition)
    }

    fun notifyTaskDisabled(position: Int) {
        tasks.value!![position].disable()
        recalculateStartTimes(position)
    }

}