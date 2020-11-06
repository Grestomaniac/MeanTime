package com.sillyapps.meantime.ui.edittemplatescreen

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.R
import com.sillyapps.meantime.convertToMillis
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

    var templateId = savedStateHandle.get<Int>("templateId")!!
    val templateName: MutableLiveData<String> = MutableLiveData("")
    val tasks: MutableLiveData<MutableList<Task>> = MutableLiveData(mutableListOf())

    val task: MutableLiveData<Task> = MutableLiveData()

    private var taskPosition = AppConstants.NOT_ASSIGNED

    init {
        if (templateId != 0)
            viewModelScope.launch {
                val template = repository.getTemplate(templateId)
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

            if (taskPosition == AppConstants.NOT_ASSIGNED) {
                it.add(newTask)
            }

            else {
                it[taskPosition] = newTask
                recalculateStartTimes(taskPosition)
            }
        }
        tasks.value = tasks.value
        taskPosition = -1
    }

    fun editTask(position: Int) {
        taskPosition = position
        task.setValue(tasks.value!![position])
    }

    fun createNewTask() {
        val nextStartTime = if (tasks.value!!.isNotEmpty()) {
            tasks.value!!.last().getNextStartTime()
        } else 0L

        task.setValue(Task(nextStartTime))
    }

    fun setTaskDuration(duration: Long) {
        task.value!!.duration = duration
    }

    fun setTaskSound(sound: String) {
        Timber.d("Setting sound $sound")
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

        val template = Template(templateId, templateName.value!!, false, tasks.value!!)
        viewModelScope.launch {
            //TODO dangerous code
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
}