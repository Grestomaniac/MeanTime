package com.sillyapps.meantime.ui.goalscreen

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.data.Goal
import com.sillyapps.meantime.data.TaskGoals
import com.sillyapps.meantime.data.repository.AppRepository
import com.sillyapps.meantime.ui.SingleLiveEvent
import kotlinx.coroutines.*
import java.util.*

class GoalViewModel @ViewModelInject constructor(private val repository: AppRepository): ViewModel() {

    private val updateInterval = 5000L

    private var updateTimer: Job? = null

    private val taskGoals: MutableLiveData<TaskGoals> = MutableLiveData()

    private val _tabSelected: MutableLiveData<Int> = MutableLiveData(0)
    val tabSelected: LiveData<Int> = _tabSelected

    val activeGoals: LiveData<MutableList<Goal>> = taskGoals.map { it.activeGoals }

    val completedGoals: LiveData<MutableList<Goal>> = taskGoals.map { it.completedGoals }

    val goal: MutableLiveData<Goal> = MutableLiveData()
    private var goalPos = AppConstants.NOT_ASSIGNED

    val newGoalAdded: SingleLiveEvent<Void> = SingleLiveEvent()

    fun load(taskGoalId: Int) {
        viewModelScope.launch {
            taskGoals.value = repository.getTaskGoals(taskGoalId)
        }
    }

    fun selectTab(tabPosition: Int) {
        _tabSelected.value = tabPosition
    }

    fun notifyActiveGoalSwapped(fromPosition: Int, toPosition: Int) {
        Collections.swap(activeGoals.value!!, fromPosition, toPosition)
        goalsChanged()
    }

    fun notifyActiveGoalRemoved(position: Int) {
        completedGoals.value!!.add(activeGoals.value!![position])
        activeGoals.value!!.removeAt(position)
        goalsChanged()
    }

    fun notifyCompletedGoalSwapped(fromPosition: Int, toPosition: Int) {
        Collections.swap(completedGoals.value!!, fromPosition, toPosition)
        goalsChanged()
    }

    fun notifyCompletedGoalRecovered(position: Int) {
        val goal = completedGoals.value!![position]
        goal.saveGoal()
        activeGoals.value!!.add(goal)
        completedGoals.value!!.removeAt(position)
        goalsChanged()
    }

    fun notifyCompletedGoalRemoved(position: Int) {
        completedGoals.value!!.removeAt(position)
        goalsChanged()
    }

    fun editGoal(position: Int) {
        goalPos = position
        if (goalPos == AppConstants.NOT_ASSIGNED) {
            goal.value = Goal()
        }
        else {
            goal.value = activeGoals.value!![position].copy()
        }
    }

    fun saveGoal() {
        goal.value!!.saveGoal()
        if (goalPos == AppConstants.NOT_ASSIGNED) {
            activeGoals.value!!.add(goal.value!!)
            newGoalAdded.call()
        }
        else {
            activeGoals.value!![goalPos].fillWith(goal.value!!)
        }
    }

    private fun goalsChanged() {
        if (updateTimer != null)
            return

        updateTimer = viewModelScope.launch {
            delay(updateInterval)

            updateTimer = null
            updateGoals()
        }
    }

    override fun onCleared() {
        updateTimer?.cancel()
        taskGoals.value?.let { updateGoals() }
        super.onCleared()
    }

    private fun updateGoals() {
        CoroutineScope(Dispatchers.IO).launch {
            repository.updateGoals(taskGoals.value!!)
        }
    }

}