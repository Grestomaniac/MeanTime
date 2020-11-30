package com.sillyapps.meantime.ui.goalscreen

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.data.Goal
import com.sillyapps.meantime.data.TaskGoals
import com.sillyapps.meantime.data.repository.AppRepository
import kotlinx.coroutines.*
import java.util.*

class GoalViewModel @ViewModelInject constructor(private val repository: AppRepository): ViewModel() {

    enum class GoalTab{ ACTIVE, COMPLETED }

    private val updateInterval = 5000L

    private var updateTimer: Job? = null

    private val taskGoals: MutableLiveData<TaskGoals> = MutableLiveData()

    private val _tabSelected: MutableLiveData<GoalTab> = MutableLiveData(GoalTab.ACTIVE)
    val tabSelected: LiveData<GoalTab> = _tabSelected

    val goals: LiveData<MutableList<Goal>> = taskGoals.map { it.activeGoals }

    val completedGoals: LiveData<MutableList<Goal>> = taskGoals.map { it.completedGoals }

    val goal: MutableLiveData<Goal> = MutableLiveData()
    private var goalPos = AppConstants.NOT_ASSIGNED

    fun load(taskGoalId: Int) {
        viewModelScope.launch {
            taskGoals.value = repository.getTaskGoals(taskGoalId)
        }
    }

    fun selectTab(goalTab: GoalTab) {
        _tabSelected.value = goalTab
    }

    fun notifyActiveGoalSwapped(fromPosition: Int, toPosition: Int) {
        Collections.swap(goals.value!!, fromPosition, toPosition)
        goalsChanged()
    }

    fun notifyActiveGoalRemoved(position: Int) {
        completedGoals.value!!.add(goals.value!![position])
        goals.value!!.removeAt(position)
        goalsChanged()
    }

    fun notifyCompletedGoalSwapped(fromPosition: Int, toPosition: Int) {
        Collections.swap(completedGoals.value!!, fromPosition, toPosition)
        goalsChanged()
    }

    fun notifyCompletedGoalRecovered(position: Int) {
        goals.value!!.add(completedGoals.value!![position])
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
            goal.value = goals.value!![position].copy()
        }
    }

    fun saveGoal() {
        goal.value!!.saveGoal()
        if (goalPos == AppConstants.NOT_ASSIGNED) {
            goals.value!!.add(goal.value!!)
        }
        else {
            goals.value!![goalPos].fillWith(goal.value!!)
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