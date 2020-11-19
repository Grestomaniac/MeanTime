package com.sillyapps.meantime.ui.mainscreen

import androidx.databinding.Observable
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.sillyapps.meantime.AppBR
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.BR
import com.sillyapps.meantime.data.AppPermissionWarnings
import com.sillyapps.meantime.data.Day
import com.sillyapps.meantime.data.State
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.ui.TimePickerViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel @ViewModelInject constructor(private val dayManager: DayManager): ViewModel(), TimePickerViewModel {

    private val currentDay: MutableLiveData<Day> = MutableLiveData()

    val tasks: MutableLiveData<MutableList<Task>> = MutableLiveData(mutableListOf())

    private val _uiTimeRemain: MutableLiveData<Long> = MutableLiveData(0)
    val uiTimeRemain: LiveData<Long> = _uiTimeRemain

    private val _serviceRunning: MutableLiveData<Boolean> = MutableLiveData(false)
    val serviceRunning: LiveData<Boolean> = _serviceRunning

    private val _task: MutableLiveData<Task> = MutableLiveData()
    val task: LiveData<Task> = _task
    private var taskPosition = AppConstants.NOT_ASSIGNED

    private val _noTemplate: MutableLiveData<Boolean> = MutableLiveData(false)
    val noTemplate: LiveData<Boolean> = _noTemplate

    private val _appPermissionWarnings: MutableLiveData<AppPermissionWarnings> = MutableLiveData(
        AppPermissionWarnings()
    )
    val appPermissionWarnings: LiveData<AppPermissionWarnings> = _appPermissionWarnings

    private val _refreshing = MutableLiveData(false)
    val refreshing: LiveData<Boolean> = _refreshing

    private val _paused = MutableLiveData(false)
    val paused: LiveData<Boolean> = _paused

    init {
        loadDay()
    }

    fun loadDay(request: DayManager.RequestType = DayManager.RequestType.GET_CURRENT) {
        viewModelScope.launch {
            // No template
            if (dayManager.loadCurrentDay(request)) {
                currentDay.value = null
                _serviceRunning.value = false
                tasks.value = null

                _noTemplate.value = true
            }
            else {
                val day = dayManager.thisDay!!
                currentDay.value = day
                _serviceRunning.value = day.isRunning
                tasks.value = day.tasks
                _noTemplate.value = false
                setDayPausedOrUnPaused()

                dayManager.thisDay!!.addOnPropertyChangedCallback(dataUpdateCallback)
            }
            _refreshing.value = false
        }
    }

    fun refreshDay() {
        _refreshing.value = true
        loadDay(DayManager.RequestType.REFRESH)
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

    fun onStopButtonLongClick(): Boolean {
        dayManager.resetDay(true)
        return true
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
        taskPosition = position
        _task.value = tasks.value!![position]
    }

    fun onTaskDialogClosed() {
        _task.value = null
        taskPosition = AppConstants.NOT_ASSIGNED
    }

    fun onTaskRevertChanges() {
        _task.value!!.revertChanges()
        recalculateStartTimes(taskPosition)
    }

    override fun setTaskDuration(duration: Long) {
        if (duration == getTaskDuration()) {
            return
        }
        _task.value!!.editableDuration = duration
        recalculateStartTimes(taskPosition)
    }

    override fun getTaskDuration(): Long {
        return _task.value!!.editableDuration
    }

    override fun onCleared() {
        Timber.d("Removing on property changed listener")
        dayManager.thisDay?.removeOnPropertyChangedCallback(dataUpdateCallback)
        super.onCleared()
    }

    private fun setDayPausedOrUnPaused() {
        _paused.value = currentDay.value!!.state == State.DISABLED
    }

    private val dataUpdateCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            when (propertyId) {
                BR.timeRemain -> {
                    _uiTimeRemain.value = currentDay.value!!.timeRemain
                }

                BR.isRunning -> {
                    _serviceRunning.value = currentDay.value!!.isRunning
                }

                AppBR.dayEnded -> {
                    loadDay(DayManager.RequestType.GET_NEXT)
                }

                AppBR.dayPausedOrUnPaused -> {
                    setDayPausedOrUnPaused()
                }
            }
        }
    }

}