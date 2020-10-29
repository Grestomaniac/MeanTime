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

class MainViewModel @ViewModelInject constructor(private val dayManager: DayManager): ViewModel() {

    private val currentDay: PropertyAwareMutableLiveData<Day> = PropertyAwareMutableLiveData()

    val tasks: LiveData<MutableList<RunningTask>> = currentDay.map { it.tasks }
    val uiTimeRemain: LiveData<Long> = currentDay.map { it.timeRemain }
    val serviceRunning: LiveData<Boolean> = currentDay.map { it.running }

    private val _noTemplate: MutableLiveData<Boolean> = MutableLiveData(false)
    val noTemplate: LiveData<Boolean> = _noTemplate

    init {
        viewModelScope.launch {
            // No template
            if (dayManager.loadCurrentDay())
                _noTemplate.value = true
            else {
                currentDay.postValue(dayManager.thisDay)
            }

        }
    }

    fun startButtonPressed() {
        dayManager.start()
    }

    fun pauseButtonPressed() {
        dayManager.pauseDay()
    }

    fun stopButtonPressed() {
        dayManager.getNextTask()
    }

    fun recalculateStartTimes(position: Int) {
        dayManager.recalculateStartTimes(position)
    }

    fun notifyTasksSwapped(upperPosition: Int, bottomPosition: Int) {
        dayManager.notifyTasksSwapped(upperPosition, bottomPosition)
    }

    fun notifyTaskDisabled(position: Int) {
        dayManager.notifyTaskDisabled(position)
    }

}