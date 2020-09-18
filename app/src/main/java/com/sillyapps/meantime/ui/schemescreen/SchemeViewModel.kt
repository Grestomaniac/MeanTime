package com.sillyapps.meantime.ui.schemescreen

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sillyapps.meantime.data.repository.AppRepository
import kotlinx.coroutines.launch

class SchemeViewModel @ViewModelInject constructor(private val repository: AppRepository): ViewModel() {

    val schemes = repository
}