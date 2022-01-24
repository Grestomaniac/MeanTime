package com.sillyapps.meantime.ui.edittemplatescreen

import androidx.lifecycle.*
import com.maltaisn.icondialog.pack.IconPack
import com.sillyapps.meantime.data.BaseTask
import com.sillyapps.meantime.data.EditableBaseTask
import com.sillyapps.meantime.data.Goals
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.data.local.BaseTaskDao
import com.sillyapps.meantime.data.local.GoalsDao
import com.sillyapps.meantime.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditBaseTaskViewModel @Inject constructor(savedStateHandle: SavedStateHandle, private val baseTaskDao: BaseTaskDao, private val goalsDao: GoalsDao, val iconPack: IconPack): ViewModel() {

    val baseTaskId = savedStateHandle.get<Long>("baseTaskId")
    val baseTask: LiveData<EditableBaseTask> = MutableLiveData(EditableBaseTask())

    init {
        if (baseTaskId != null && baseTaskId != 0L) {
            viewModelScope.launch {
                val task = baseTaskDao.getBaseTask(baseTaskId) ?: return@launch
                baseTask.value?.fillData(task)
            }
        }
    }

    fun setTaskIcon(iconId: Int) {
        baseTask.value!!.iconResId = iconId
    }

    fun isDataValid(): EditableBaseTask.WhatIsWrong {
        return baseTask.value!!.validateData()
    }

    fun save() {
        viewModelScope.launch {
            val id = baseTaskId ?: 0L

            val task = baseTask.value!!.toBaseTask(id)
            val taskId = baseTaskDao.upsert(task)

            if (id == 0L) {
                goalsDao.insert(Goals(baseTaskId = taskId))
            }
        }
    }

    fun getDuration(): Long {
        return baseTask.value!!.defaultDuration
    }

    fun setDuration(duration: Long) {
        baseTask.value!!.defaultDuration = duration
    }

    fun setTaskBreak(taskBreak: Task.Break) {
        baseTask.value!!.defaultBreakInterval = taskBreak
    }

}