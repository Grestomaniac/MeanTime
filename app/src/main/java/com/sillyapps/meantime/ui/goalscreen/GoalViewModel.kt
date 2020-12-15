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
    val viewModelLoaded: SingleLiveEvent<Void> = SingleLiveEvent()

    fun load(taskGoalId: Int) {
        viewModelScope.launch {
            taskGoals.value = repository.getTaskGoals(taskGoalId)
            viewModelLoaded.call()
        }
    }

    fun selectTab(tabPosition: Int) {
        _tabSelected.value = tabPosition
    }

    fun getDefaultGoalPosition(): Int {
        return taskGoals.value!!.defaultGoalPos
    }

    fun notifyActiveGoalRemoved(position: Int) {
        if (position == taskGoals.value!!.defaultGoalPos) {
            taskGoals.value!!.unselectDefaultGoal()
        }
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

    fun notifyTaskSelected(position: Int) {
        taskGoals.value!!.selectDefaultGoal(position)
        goalsChanged()
    }

    fun createNewGoal() {
        goalPos = AppConstants.NOT_ASSIGNED
        goal.value = Goal()
    }

    fun editActiveGoal(position: Int) {
        goalPos = position
        goal.value = activeGoals.value!![position].copy()
    }

    fun editCompletedGoal(position: Int) {
        goalPos = activeGoals.value!!.size + position
        goal.value = completedGoals.value!![position].copy()
    }

    fun saveGoal() {
        goal.value!!.saveGoal()
        if (goalPos == AppConstants.NOT_ASSIGNED) {
            activeGoals.value!!.add(goal.value!!)
            newGoalAdded.call()
        }
        else if (goalPos < activeGoals.value!!.size) {
            activeGoals.value!![goalPos].fillWith(goal.value!!)
        }
        else {
            completedGoals.value!![activeGoals.value!!.size-goalPos]
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