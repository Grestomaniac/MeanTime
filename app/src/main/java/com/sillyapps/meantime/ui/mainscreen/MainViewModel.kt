package com.sillyapps.meantime.ui.mainscreen

import androidx.databinding.Observable
import androidx.lifecycle.*
import com.sillyapps.meantime.AppBR
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.BR
import com.sillyapps.meantime.data.AppPermissionWarnings
import com.sillyapps.meantime.data.State
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.data.BaseTask
import com.sillyapps.meantime.ui.SingleLiveEvent
import com.sillyapps.meantime.utils.removeExtraSpaces
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val dayManager: DayManager): ViewModel() {

    val uiTasks: MutableLiveData<MutableList<Task>> = MutableLiveData(mutableListOf())

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

    val baseTask: LiveData<List<BaseTask>> = dayManager.observeTaskGoals()

    private val dataUpdateCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            when (propertyId) {
                BR.timeRemain -> dayManager.thisDay!!.updateTaskRelativeProgress()

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
                addOnPropertyChangedCallback(dataUpdateCallback)
                updateTaskRelativeProgress()
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

    fun startOrPauseButtonPressed() {
        if (dayManager.thisDay!!.dayState == State.ACTIVE) dayManager.pauseDay()
        else dayManager.start()
    }

    fun stopButtonPressed() {
        dayManager.stopTask()
    }

    fun createTemporalTask() {
        taskPosition = AppConstants.NOT_ASSIGNED
        _task.value = Task(temporal = true)
    }

    fun onTaskClicked(position: Int) {
        taskPosition = position
        _task.value = uiTasks.value!![position].copy()
    }

    fun onTaskDialogClosed() {
        _task.value = null
        taskPosition = AppConstants.NOT_ASSIGNED
    }

    fun saveTask() {
        _task.value?.let {
            if (taskPosition == AppConstants.NOT_ASSIGNED) {
                addTemporalTask(it)
            }
            else {
                uiTasks.value!![taskPosition].copyDataFrom(it)
                recalculateStartTimes(taskPosition+1)
            }
        }
        if (taskPosition == dayManager.thisDay!!.currentTaskPos) {
            dayManager.setProperTicker()
        }
    }

    private fun addTemporalTask(task: Task) {
        viewModelScope.launch {
            task.name = removeExtraSpaces(task.name)
            task.goalsId = dayManager.getTaskGoalsIdByName(task.name)
            dayManager.thisDay!!.addTemporalTask(task)
        }
    }

    fun validateTaskData(): Task.WhatIsWrong {
        return task.value!!.isDataValid()
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

    fun getTemplateName(): String? {
        return dayManager.thisDay?.name
    }

    fun setTaskDuration(duration: Long) {
        task.value!!.editableDuration = duration
    }

    override fun onCleared() {
        dayManager.thisDay?.removeOnPropertyChangedCallback(dataUpdateCallback)
        super.onCleared()
    }

}