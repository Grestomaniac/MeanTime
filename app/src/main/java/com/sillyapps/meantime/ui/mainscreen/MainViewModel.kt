package com.sillyapps.meantime.ui.mainscreen

import androidx.databinding.Observable
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.sillyapps.meantime.AppBR
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.BR
import com.sillyapps.meantime.data.AppPermissionWarnings
import com.sillyapps.meantime.data.State
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.data.TaskGoals
import com.sillyapps.meantime.ui.SingleLiveEvent
import com.sillyapps.meantime.ui.TimePickerViewModel
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(private val dayManager: DayManager): ViewModel(), TimePickerViewModel {

    val uiTasks: MutableLiveData<MutableList<Task>> = MutableLiveData(mutableListOf())

    private val _uiTimeRemain: MutableLiveData<Long> = MutableLiveData(0)
    val uiTimeRemain: LiveData<Long> = _uiTimeRemain

    private val _serviceRunning: MutableLiveData<Boolean> = MutableLiveData(false)
    val serviceRunning: LiveData<Boolean> = _serviceRunning

    private val _uiDayState: MutableLiveData<State> = MutableLiveData()
    val uiDayState: LiveData<State> = _uiDayState

    private val _task: MutableLiveData<Task> = MutableLiveData()
    val task: LiveData<Task> = _task
    private var taskPosition = AppConstants.NOT_ASSIGNED

    val currentTaskStateChanged: SingleLiveEvent<Void> = SingleLiveEvent()

    val taskAdded: SingleLiveEvent<Void> = SingleLiveEvent()

    val dayPaused: SingleLiveEvent<Void> = SingleLiveEvent()

    private val _appPermissionWarnings: MutableLiveData<AppPermissionWarnings> = MutableLiveData(
        AppPermissionWarnings()
    )
    val appPermissionWarnings: LiveData<AppPermissionWarnings> = _appPermissionWarnings

    private val _refreshing = MutableLiveData(false)
    val refreshing: LiveData<Boolean> = _refreshing

    private val _paused = MutableLiveData(false)
    val paused: LiveData<Boolean> = _paused

    val taskGoals: LiveData<List<TaskGoals>> = dayManager.observeTaskGoals()

    private val dataUpdateCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            when (propertyId) {
                BR.timeRemain -> _uiTimeRemain.value = dayManager.thisDay!!.timeRemain

                BR.isRunning -> _serviceRunning.value = dayManager.thisDay!!.isRunning

                BR.dayState -> _uiDayState.value = dayManager.thisDay!!.dayState

                AppBR.currentTaskStateChanged -> currentTaskStateChanged.call()

                AppBR.taskAdded -> taskAdded.call()

                AppBR.dayPaused -> dayPaused.call()

                AppBR.dayEnded -> loadDay(DayManager.RequestType.GET_NEXT)
            }
        }
    }

    init {
        loadDay()
    }

    fun loadDay(request: DayManager.RequestType = DayManager.RequestType.GET_CURRENT) {
        viewModelScope.launch {
            dayManager.loadCurrentDay(request)

            dayManager.thisDay?.apply {
                _serviceRunning.value = isRunning
                uiTasks.value = tasks
                _uiDayState.value = dayState
                _uiTimeRemain.value = timeRemain
                addOnPropertyChangedCallback(dataUpdateCallback)
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
        dayManager.stopTask()
    }

    fun createTemporalTask() {
        _task.value = Task(temporal = true)
    }

    fun validateTaskData(): Task.WhatIsWrong {
        return task.value!!.isDataValid()
    }

    fun addTemporalTask() {
        _task.value?.let {
            viewModelScope.launch {
                it.goalsId = dayManager.getTaskGoalsIdByName(it.name)
                dayManager.thisDay!!.addTemporalTask(it)
            }
        }
    }

    fun onStopButtonLongClick(): Boolean {
        dayManager.resetDay()
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
        _task.value = uiTasks.value!![position]
    }

    fun onTaskDialogClosed() {
        _task.value = null
        taskPosition = AppConstants.NOT_ASSIGNED
    }

    fun onTaskRevertChanges() {
        if (_task.value!!.canNotBeSwappedOrDisabled()) return

        _task.value!!.revertChanges()
        recalculateStartTimes(taskPosition)
    }

    fun getCurrentTaskGoalsId(): Int? {
        if (dayManager.currentTaskGoalsIsNotEmpty) {
            return dayManager.thisDay!!.currentTask.goalsId
        }
        return null
    }

    fun getCurrentTaskPosition(): Int {
        return dayManager.thisDay!!.currentTaskPos
    }

    fun setTemporalTaskDuration(duration: Long) {
        task.value!!.duration = duration
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
        dayManager.thisDay?.removeOnPropertyChangedCallback(dataUpdateCallback)
        super.onCleared()
    }

    private fun pauseDay() {
        _paused.value = dayManager.thisDay!!.dayState == State.DISABLED
    }

}