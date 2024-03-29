package com.sillyapps.meantime.ui.edittemplatescreen

import android.graphics.drawable.Drawable
import androidx.lifecycle.*
import com.maltaisn.icondialog.pack.IconPack
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.R
import com.sillyapps.meantime.data.BaseTask
import com.sillyapps.meantime.data.SimpleBaseTask
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.data.Template
import com.sillyapps.meantime.data.repository.AppRepository
import com.sillyapps.meantime.ui.Result
import com.sillyapps.meantime.utils.convertToMillis
import com.sillyapps.meantime.utils.removeExtraSpaces
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EditTemplateViewModel @Inject constructor(private val repository: AppRepository,
                                                private val savedStateHandle: SavedStateHandle,
                                                val iconPack: IconPack): ViewModel() {

    var templateId = savedStateHandle.get<Int>("templateId")!!
    val templateName: MutableLiveData<String> = MutableLiveData("")
    val tasks: MutableLiveData<MutableList<Task>> = MutableLiveData(mutableListOf())

    private val goalTasks: LiveData<List<BaseTask>> = repository.observeAllBaseTasks()
    val goalTasksNames: LiveData<List<String>> = goalTasks.map { it.map { goal -> goal.name } }

    val tasksBaseTasks: LiveData<List<BaseTask?>> = MediatorLiveData<List<BaseTask?>>().apply {
        addSource(tasks) {
            Timber.d("tasks changed")
            value = tasks.value?.map { task -> goalTasks.value?.find { it.id == task.goalsId } } }
        addSource(goalTasks) {
            Timber.d("GoalTasks changed")
            value = tasks.value?.map { task -> goalTasks.value?.find { it.id == task.goalsId } } }
    }

    val task: MutableLiveData<Task> = MutableLiveData()

    val taskIconResId: MutableLiveData<Int?> = MutableLiveData()
    val baseTaskData: MutableLiveData<BaseTask> = MutableLiveData()

    private var taskPosition = AppConstants.NOT_ASSIGNED

    init {
        if (templateId != 0)
            viewModelScope.launch {
                val template = repository.loadTemplate(templateId)
                templateName.postValue(template!!.name)
                tasks.postValue(template.activities)
            }
    }

    fun populate() {
        for (i in 0 until 5) {
            createNewTask()
            task.value?.let { task ->
                task.duration = convertToMillis(1, 0)
                task.name = "Task$i"
            }
            addCreatedTask()
        }
        templateName.value = "Default"
    }

    fun addCreatedTask() {
        tasks.value!!.let {
            val newTask = task.value!!
            newTask.name = removeExtraSpaces(newTask.name)

            updateBaseTask()

            if (taskPosition == AppConstants.NOT_ASSIGNED) {
                it.add(newTask)
            }

            else {
                it[taskPosition] = newTask
                recalculateStartTimes(taskPosition)
            }
        }

        taskPosition = AppConstants.NOT_ASSIGNED
    }

    private fun updateBaseTask() {
        viewModelScope.launch {
            task.value?.goalsId = repository.getBaseTaskIdByName(task.value!!.name)
            repository.updateSimpleBaseTask(task.value, taskIconResId.value ?: -1)
            taskIconResId.value = null
        }
    }

    fun editTask(position: Int) {
        taskPosition = position
        val taskCopy = tasks.value!![position].copy()

        viewModelScope.launch {
            Timber.d("BaseTask id = ${taskCopy.goalsId}")
            val baseTask = repository.getBaseTask(taskCopy.goalsId)
            val iconRes = baseTask?.iconResId
            taskIconResId.postValue(iconRes)
        }
        task.value = taskCopy
    }

    fun createNewTask() {
        val nextStartTime = if (tasks.value!!.isNotEmpty()) {
            tasks.value!!.last().getNextStartTime()
        } else 0L

        task.value = Task(nextStartTime)
    }

    fun setTaskIcon(iconResId: Int?) {
        taskIconResId.value = iconResId
    }

    fun getTaskDuration(): Long {
        return task.value!!.duration
    }

    fun setTaskDuration(duration: Long) {
        task.value!!.duration = duration
    }

    fun setTaskSound(sound: String) {
        task.value!!.sound = sound
    }

    fun isTaskDataValid(): Task.WhatIsWrong {
        return task.value!!.isDataValid()
    }

    fun saveTemplate(): Result {
        if (tasks.value!!.size == 0) {
            return Result(false, R.string.zero_tasks)
        }
        if (templateName.value == "") {
            return Result(false, R.string.name_is_empty)
        }

        viewModelScope.launch {
            for (task in tasks.value!!) {
                if (task.goalsId == 0L) {
                    task.goalsId = repository.getBaseTaskIdByName(task.name)
                }
            }

            val template = Template(templateId, templateName.value!!, false, tasks.value!!)

            templateId = repository.insertTemplate(template)
        }
        return Result(true)

    }

    fun recalculateStartTimes(position: Int) {
        tasks.value?.let {
            var pos = position
            if (pos == 0) {
                it[pos].startTime = 0L
                pos++
            }
            for (i in pos until it.size) {
                it[i].startTime = it[i-1].getNextStartTime()
            }
        }

    }

    fun notifyTasksSwapped(upperPosition: Int, bottomPosition: Int) {
        Collections.swap(tasks.value!!, bottomPosition, upperPosition)
    }

    fun notifyTaskRemoved(position: Int) {
        tasks.value!!.removeAt(position)
        recalculateStartTimes(position)
    }

    fun getTaskBreak(): Task.Break {
        return task.value!!.taskBreak
    }

    fun setTaskBreak(taskBreak: Task.Break) {
        task.value!!.taskBreak = taskBreak
    }

    fun setBaseTask(baseTask: SimpleBaseTask) {
        task.value?.apply{
            goalsId = baseTask.id
            name = baseTask.name
            taskIconResId.value = baseTask.iconResId
        }
    }

    fun getDrawableForIcon(iconId: Int?): Drawable? {
        if (iconId == null) return null

        return iconPack.getIcon(iconId)?.drawable
    }
}