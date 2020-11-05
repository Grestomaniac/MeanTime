package com.sillyapps.meantime.ui.mainscreen

import androidx.databinding.Observable
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.sillyapps.meantime.BR
import com.sillyapps.meantime.data.AppPermissionWarnings
import com.sillyapps.meantime.data.Day
import com.sillyapps.meantime.data.PropertyAwareMutableLiveData
import com.sillyapps.meantime.data.RunningTask
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(private val dayManager: DayManager): ViewModel() {

    private val currentDay: PropertyAwareMutableLiveData<Day> = PropertyAwareMutableLiveData()

    val tasks: LiveData<MutableList<RunningTask>> = currentDay.map { it.tasks }
    private val _uiTimeRemain: MutableLiveData<Long> = MutableLiveData(0)
    val uiTimeRemain: LiveData<Long> = _uiTimeRemain
    private val _serviceRunning: MutableLiveData<Boolean> = MutableLiveData(false)
    val serviceRunning: LiveData<Boolean> = _serviceRunning

    private val _task: PropertyAwareMutableLiveData<RunningTask> = PropertyAwareMutableLiveData()
    val task: LiveData<RunningTask> = _task

    private val _noTemplate: MutableLiveData<Boolean> = MutableLiveData(false)
    val noTemplate: LiveData<Boolean> = _noTemplate

    private val _appPermissionWarnings: MutableLiveData<AppPermissionWarnings> = MutableLiveData(
        AppPermissionWarnings()
    )
    val appPermissionWarnings: LiveData<AppPermissionWarnings> = _appPermissionWarnings

    fun loadDay() {
        viewModelScope.launch {
            // No template
            if (dayManager.loadCurrentDay()) {
                _noTemplate.value = true
            }
            else {
                currentDay.value = dayManager.thisDay!!
                _serviceRunning.value = currentDay.value!!.runningState
                _noTemplate.value = false
                dayManager.thisDay!!.addOnPropertyChangedCallback(dataUpdateCallback)
            }
        }
    }

    fun updatePermissionWarnings(ignoresAppOptimizations: Boolean, notificationEnabled: Boolean) {
        _appPermissionWarnings.value?.apply {
            batteryOptimizationEnabled = !ignoresAppOptimizations
            notificationDisabled = !notificationEnabled
        }
    }

    fun startButtonPressed() {
        dayManager.start()
    }

    fun pauseButtonPressed() {
        dayManager.pauseDay()
    }

    fun stopButtonPressed() {
        dayManager.getNextTask(true)
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

    fun onTaskClicked(position: Int) {
        _task.value = tasks.value!![position]
    }

    fun onTaskDialogClosed() {
        _task.value = null
    }

    override fun onCleared() {
        dayManager.thisDay?.removeOnPropertyChangedCallback(dataUpdateCallback)
        super.onCleared()
    }

    private val dataUpdateCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            when (propertyId) {
                BR.timeRemain -> {
                    _uiTimeRemain.value = currentDay.value!!.timeRemain
                }
                BR.runningState -> {
                    _serviceRunning.value = currentDay.value!!.runningState
                }

            }
        }
    }

}