package com.sillyapps.meantime.ui.mainscreen

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.sillyapps.meantime.data.Day
import com.sillyapps.meantime.data.PropertyAwareMutableLiveData
import com.sillyapps.meantime.data.Template
import com.sillyapps.meantime.data.repository.AppRepository
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(private val repository: AppRepository): ViewModel() {

    val currentDay: PropertyAwareMutableLiveData<Day> = PropertyAwareMutableLiveData()

    init {
        viewModelScope.launch {
            currentDay.postValue(repository.getCurrentDay())
            if (currentDay.value == null) {
                //No templates navigate to templateEditor
            }
        }
    }

    fun startNewDay() {
        //Todo start service
    }

    fun endDay() {
        //Todo stop service
    }

}