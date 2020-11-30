package com.sillyapps.meantime.ui.alarmscreen

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.data.State
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.ui.mainscreen.DayManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmViewModel @ViewModelInject constructor(private val dayManager: DayManager): ViewModel() {

    lateinit var completedTask: Task
    var dayEnded: Boolean = false
    var nextTaskName = ""
    var alarmDuration: Long = 0L

    private val _timeIsUp: MutableLiveData<Boolean> = MutableLiveData(false)
    val timeIsUp: LiveData<Boolean> = _timeIsUp

    private var timerJob: Job? = null

    fun reload() {
        timerJob?.cancel()

        val day = dayManager.thisDay!!
        dayEnded = (day.dayState == State.COMPLETED)
        completedTask = day.getCompletedTask()
        alarmDuration = AppConstants.ALARM_DEFAULT_DURATION

        if (!dayEnded) {
            nextTaskName = day.getCompletedTask(1).name
        }

        timerJob = viewModelScope.launch {
            delay(alarmDuration)
            _timeIsUp.value = true
        }
    }
}