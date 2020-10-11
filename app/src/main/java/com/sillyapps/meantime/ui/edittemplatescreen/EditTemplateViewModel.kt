package com.sillyapps.meantime.ui.edittemplatescreen

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.sillyapps.meantime.R
import com.sillyapps.meantime.UNCERTAIN
import com.sillyapps.meantime.convertToMillis
import com.sillyapps.meantime.data.EditableTask
import com.sillyapps.meantime.data.PropertyAwareMutableLiveData
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.data.Template
import com.sillyapps.meantime.data.repository.AppRepository
import com.sillyapps.meantime.ui.Result
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class EditTemplateViewModel @ViewModelInject constructor(private val repository: AppRepository,
                                                         @Assisted private val savedStateHandle: SavedStateHandle): ViewModel() {
    private val NEW_ONE = -1

    val templateId = savedStateHandle.get<Int>("templateId")!!
    val templateName: MutableLiveData<String> = MutableLiveData("")
    val tasks: MutableLiveData<MutableList<Task>> = MutableLiveData(mutableListOf())
    var nextStartTime = 0L

    val task: PropertyAwareMutableLiveData<EditableTask> = PropertyAwareMutableLiveData()
    val duration: LiveData<String> = task.map { it.editableUiDuration }

    var taskPosition = NEW_ONE

    init {
        if (templateId != 0)
            viewModelScope.launch {
                val template = repository.getTemplate(templateId)
                templateName.postValue(template!!.name)
                tasks.postValue(template.activities)
                nextStartTime = template.nextStartTime
            }
    }

    fun populateTasks() {
        for (i in 0 until 5) {
            createNewTask()
            task.value?.let { task ->
                task.editableDuration = convertToMillis(1, 0)
                task.editableName = "Task$i"
            }
            addCreatedTask()
        }
    }

    fun addCreatedTask() {
        tasks.value!!.let {
            val newTask = Task.createFromEditableTask(task.value!!)

            if (taskPosition == NEW_ONE) {
                it.add(newTask)
                if (newTask.duration != UNCERTAIN)
                    nextStartTime += newTask.duration
                else {
                    nextStartTime = UNCERTAIN
                }
            }

            else {
                it[taskPosition] = newTask
                recalculateStartTimes(taskPosition)
            }
        }
        taskPosition = -1
    }

    fun editTask(position: Int) {
        taskPosition = position
        task.setValue(EditableTask.copyFromExistingTask(tasks.value!![position]))
    }

    fun createNewTask() {
        task.setValue(EditableTask(nextStartTime))
    }

    fun setDuration(hours: Int, minutes: Int) {
        task.value!!.setDuration(hours, minutes)
    }

    fun isTaskDataValid(): EditableTask.WhatIsWrong {
        return task.value!!.isDataValid()
    }

    fun saveTemplate(): Result {
        if (tasks.value!!.size == 0) {
            return Result(false, R.string.zero_tasks)
        }
        if (templateName.value == "") {
            return Result(false, R.string.name_is_empty)
        }
        val template = Template(templateId, templateName.value!!, nextStartTime, tasks.value!!)
        viewModelScope.launch {
            repository.insertTemplate(template)
        }
        return Result(true)
    }

    fun recalculateStartTimes(position: Int) {
        tasks.value?.let {
            var pos = position
            if (pos == 0) {
                it[pos].updateStartTime(0L)
                pos++
            }
            for (i in pos until it.size) {
                it[i].updateStartTime(it[i-1].getNextStartTime())
            }
        }
    }

    fun notifyTasksSwapped(upperPosition: Int, bottomPosition: Int) {
        Collections.swap(tasks.value!!, upperPosition, bottomPosition)
    }

    fun notifyTaskRemoved(position: Int) {
        tasks.value!!.removeAt(position)
        recalculateStartTimes(position)
    }
}