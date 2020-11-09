package com.sillyapps.meantime.ui.alarmscreen

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sillyapps.meantime.data.Day
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.ui.mainscreen.DayManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmViewModel @ViewModelInject constructor(private val dayManager: DayManager): ViewModel() {

    lateinit var currentTask: Task
    var dayEnded: Boolean = false
    var nextTaskName = ""
    var alarmDuration: Long = 0L

    private val _timeIsUp: MutableLiveData<Boolean> = MutableLiveData(false)
    val timeIsUp: LiveData<Boolean> = _timeIsUp

    private var timerJob: Job? = null

    fun reload() {
        timerJob?.cancel()

        val day = dayManager.thisDay!!
        dayEnded = (day.state == Day.DayState.COMPLETED)
        currentTask = day.getPreviousTask()
        alarmDuration = day.alarmDuration

        if (!dayEnded) {
            nextTaskName = day.currentTask.name
        }
        else {
            currentTask = day.currentTask
        }

        timerJob = viewModelScope.launch {
            delay(alarmDuration)
            _timeIsUp.value = true
        }
    }
}