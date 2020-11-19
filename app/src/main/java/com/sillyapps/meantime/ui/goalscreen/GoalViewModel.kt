package com.sillyapps.meantime.ui.goalscreen

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.sillyapps.meantime.data.Goal
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.data.TaskGoals
import com.sillyapps.meantime.data.repository.AppRepository
import kotlinx.coroutines.*

class GoalViewModel @ViewModelInject constructor(private val repository: AppRepository): ViewModel() {

    private val updateInterval = 5000L

    private var updateTimer: Job? = null

    private val taskGoals: MutableLiveData<TaskGoals> = MutableLiveData()

    val goals: LiveData<MutableList<Goal>> = taskGoals.map { it.goals }

    fun load(taskGoalId: Int) {
        viewModelScope.launch {
            taskGoals.postValue(repository.getTaskGoals(taskGoalId))
        }
    }

    fun notifyTasksSwapped(upperPosition: Int, bottomPosition: Int) {
        goalsChanged()

    }

    fun notifyTaskRemoved(position: Int) {
        goalsChanged()

    }

    private fun goalsChanged() {
        if (updateTimer != null)
            return

        updateTimer = viewModelScope.launch {
            delay(updateInterval)

            updateGoals()
            updateTimer = null
        }
    }

    override fun onCleared() {
        updateGoals()
        super.onCleared()
    }

    private fun updateGoals() {
        CoroutineScope(Dispatchers.IO).launch {
            repository.updateGoals(taskGoals.value!!)
        }
    }

}