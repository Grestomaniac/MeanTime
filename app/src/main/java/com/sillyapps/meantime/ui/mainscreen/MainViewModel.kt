package com.sillyapps.meantime.ui.mainscreen

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.sillyapps.meantime.convertMillisToStringFormatWithSeconds
import com.sillyapps.meantime.data.Day
import com.sillyapps.meantime.data.PropertyAwareMutableLiveData
import com.sillyapps.meantime.data.RunningTask
import com.sillyapps.meantime.data.repository.AppRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel @ViewModelInject constructor(private val repository: AppRepository): ViewModel() {

    val currentDay: PropertyAwareMutableLiveData<Day> = PropertyAwareMutableLiveData()
    val tasks: LiveData<MutableList<RunningTask>> = currentDay.map { it.tasks }
    val currentTask: LiveData<RunningTask> = currentDay.map { it.getCurrentTask() }
    var running: Boolean = false

    private val timeRemain: MutableLiveData<Long> = MutableLiveData(0)
    val uiTimeRemain: LiveData<String> = timeRemain.map {
        convertMillisToStringFormatWithSeconds(timeRemain.value!!) }

    private val _noTemplate: MutableLiveData<Boolean> = MutableLiveData(false)
    val noTemplate: LiveData<Boolean> = _noTemplate



    private var timerJob: Job? = null

    override fun onCleared() {
        super.onCleared()
        Timber.d("ViewModel cleared")
    }

    fun preInitialize() {
        if (running) return

        viewModelScope.launch {
            Timber.d("ViewModel launched")
            val day = repository.getCurrentDay()
            currentDay.postValue(day)

            if (day == null) {
                //No templates show navigate to templateEditor
                _noTemplate.value = true
                Timber.d("No template, removing observers")
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
                    getNextTask()
                }
                else
                    updateTimeRemained(timeRemained)
                delay(1000)
            }
        }
    }

    fun pauseDay() {
        timerJob?.cancel()
        currentDay.value!!.pause()
    }

    fun getNextTask() {
        currentDay.value!!.let {
            if (it.atLastTask()) {
                resetDay()
                return
            }

            currentDay.value!!.selectNextTask(true)
            updateTimeRemained(currentTask.value!!.duration)
        }
    }

    private fun resetDay() {
        timerJob?.cancel()
        currentDay.value!!.endDay()
        updateTimeRemained(0)
        return
    }

    private fun updateTimeRemained(time: Long) {
        timeRemain.value = time
    }

    fun recalculateStartTimes(position: Int) {
        currentDay.value!!.recalculateStartTimes(position)
    }

    fun notifyTasksSwapped(upperPosition: Int, bottomPosition: Int) {
        currentDay.value!!.notifyTasksSwapped(upperPosition, bottomPosition)
    }

    fun notifyTaskDisabled(position: Int) {
        currentDay.value!!.notifyTaskDisabled(position)
    }

}