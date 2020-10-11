package com.sillyapps.meantime.ui.mainscreen

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.sillyapps.meantime.data.Day
import com.sillyapps.meantime.data.Template
import com.sillyapps.meantime.data.repository.AppRepository

class MainScreenViewModel @ViewModelInject constructor(private val repository: AppRepository): ViewModel() {

    val currentDay: LiveData<Day> = repository.currentDay

    fun startNewDay() {
        repository.startNewDay()

        //Todo start service
    }

    fun stopDay() {
        repository.stopDay()

        //Todo stop service
    }

}