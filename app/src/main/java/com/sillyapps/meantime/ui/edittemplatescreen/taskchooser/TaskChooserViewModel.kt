package com.sillyapps.meantime.ui.edittemplatescreen.taskchooser

import androidx.lifecycle.ViewModel
import com.maltaisn.icondialog.pack.IconPack
import com.sillyapps.meantime.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskChooserViewModel @Inject constructor(private val repository: AppRepository, val iconPack: IconPack?): ViewModel() {

    val baseTasks = repository.observeAllBaseTasksSimple()

}