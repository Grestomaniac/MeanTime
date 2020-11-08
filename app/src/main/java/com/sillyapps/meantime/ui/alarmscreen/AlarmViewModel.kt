package com.sillyapps.meantime.ui.alarmscreen

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sillyapps.meantime.data.Day
import com.sillyapps.meantime.data.RunningTask
import com.sillyapps.meantime.data.TimeWithSeconds
import com.sillyapps.meantime.ui.mainscreen.DayManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class AlarmViewModel @ViewModelInject constructor(private val dayManager: DayManager): ViewModel() {

    lateinit var currentTask: RunningTask
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
            nextTaskName = day.getCurrentTask().name
        }
        else {
            currentTask = day.getCurrentTask()
        }

        timerJob = viewModelScope.launch {
            delay(alarmDuration)
            _timeIsUp.value = true
        }
    }
}