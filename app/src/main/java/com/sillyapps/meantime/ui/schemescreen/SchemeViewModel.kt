package com.sillyapps.meantime.ui.schemescreen

import androidx.databinding.ObservableInt
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.sillyapps.meantime.data.Scheme
import com.sillyapps.meantime.data.Template
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class SchemeViewModel @ViewModelInject constructor(private val templatesManager: TemplatesManager): ViewModel() {

    private var scheme: Scheme? = null

    var schemeTemplates: MutableLiveData<List<Template?>> = MutableLiveData()

    init {
        viewModelScope.launch {
            templatesManager.loadTemplates()
            scheme = templatesManager.getCurrentScheme()
            schemeTemplates.value = scheme?.let { _scheme ->
                _scheme.orderList.map { templatesManager.allTemplates!!.find { template -> template.id == it } } }
        }
    }

    fun notifyItemsSwapped(fromPosition: Int, toPosition: Int) {
        Collections.swap(scheme!!.orderList, fromPosition, toPosition)
    }

    fun notifyItemRemoved(position: Int) {
        scheme!!.orderList.removeAt(position)
    }

    fun addTemplate(id: Int) {
        scheme!!.orderList.add(id)
    }
}