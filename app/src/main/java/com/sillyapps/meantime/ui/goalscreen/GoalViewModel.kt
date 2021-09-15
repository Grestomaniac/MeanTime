package com.sillyapps.meantime.ui.goalscreen

import androidx.lifecycle.*
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.data.*
import com.sillyapps.meantime.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class GoalViewModel @Inject constructor(private val repository: AppRepository): ViewModel() {

    private val updateInterval = 5000L

    private var updateTimer: Job? = null

    private val baseTask: MutableLiveData<BaseTask> = MutableLiveData()

    private val _tabSelected: MutableLiveData<Int> = MutableLiveData(0)
    val tabSelected: LiveData<Int> = _tabSelected

    val consolidatedList: MutableLiveData<ArrayList<ListItem>> = MutableLiveData(createConsolidatedList(baseTask.value!!.tagGroups))

    val goal: MutableLiveData<Goal> = MutableLiveData()
    private var goalPos = AppConstants.NOT_ASSIGNED

    fun load(baseTaskId: Int) {
        viewModelScope.launch {
            baseTask.value = repository.getBaseTask(baseTaskId)
        }
    }

    fun notifyGoalRemoved(position: Int) {
        val goalItem = consolidatedList.value!![position] as GoalItem
        baseTask.value?.removeGoal(goalItem.goal)
        updateConsolidatedList()
        goalsChanged()
    }

    private fun createConsolidatedList(tagList: List<Tag>): ArrayList<ListItem> {
        val consolidatedList = ArrayList<ListItem>()

        for (tag in tagList) {
            consolidatedList.add(TagItem(tag.name))

            val groupGoals = tag.contents
            for (i in 0 until groupGoals.size) {
                consolidatedList.add(GoalItem(groupGoals[i]))
            }
        }
        return consolidatedList
    }

    private fun updateConsolidatedList() {
        consolidatedList.value = createConsolidatedList(baseTask.value!!.tagGroups)
    }

    fun createNewGoal() {
        goalPos = AppConstants.NOT_ASSIGNED
        goal.value = Goal()
    }

    fun editGoal(position: Int) {
        goalPos = position
        goal.value = (consolidatedList.value!![position] as GoalItem).goal.copy()
    }

    fun saveGoal() {
        goal.value!!.saveGoal()
        if (goalPos == AppConstants.NOT_ASSIGNED) {
            baseTask.value!!.addGoal(goal.value!!)
            updateConsolidatedList()
        }
        else {
            (consolidatedList.value!![goalPos] as GoalItem).goal.fillWith(goal.value!!)
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
        baseTask.value?.let { updateGoals() }
        super.onCleared()
    }

    private fun updateGoals() {
        CoroutineScope(Dispatchers.IO).launch {
            repository.updateBaseTask(baseTask.value!!)
        }
    }

}