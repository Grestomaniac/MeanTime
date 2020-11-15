package com.sillyapps.meantime.ui.schemescreen

import androidx.databinding.ObservableInt
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.sillyapps.meantime.data.Scheme
import com.sillyapps.meantime.data.SimplifiedTemplate
import com.sillyapps.meantime.data.Template
import com.sillyapps.meantime.data.repository.AppRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class SchemeViewModel @ViewModelInject constructor(private val repository: AppRepository): ViewModel() {

    var scheme = MutableLiveData<Scheme>()

    var schemeTemplates: LiveData<List<SimplifiedTemplate>> = scheme.map { it.orderList }

    init {
        viewModelScope.launch {
            scheme.postValue(repository.getCurrentScheme())
        }
    }

    fun notifyItemsSwapped(fromPosition: Int, toPosition: Int) {
        Collections.swap(scheme.value!!.orderList, fromPosition, toPosition)
    }

    fun notifyItemRemoved(position: Int) {
        scheme.value!!.orderList.removeAt(position)
    }

    fun notifyItemDisabled(position: Int) {
        scheme.value!!.orderList[position].disable()
    }

    fun addTemplate(template: SimplifiedTemplate) {
        scheme.value!!.orderList.add(template)
    }

    fun saveScheme() {
        viewModelScope.launch { repository.updateCurrentScheme(scheme.value!!) }
    }

    override fun onCleared() {
        Timber.d("SchemeViewModel is cleared")
        super.onCleared()
    }
}