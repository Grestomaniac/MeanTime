package com.sillyapps.meantime.ui.mainscreen

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.sillyapps.meantime.data.Template
import com.sillyapps.meantime.data.repository.AppRepository

class MainScreenViewModel @ViewModelInject constructor(private val repository: AppRepository): ViewModel() {

    private val _currentTemplate: LiveData<Template> = repository.getCurrentTemplate()
    val currentTemplate: LiveData<Template> = _currentTemplate



}