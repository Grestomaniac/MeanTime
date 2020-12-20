package com.sillyapps.meantime.ui.schemescreen

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.sillyapps.meantime.data.Scheme
import com.sillyapps.meantime.data.SchemeTemplate
import com.sillyapps.meantime.data.SchemeTemplateInfo
import com.sillyapps.meantime.data.Template
import com.sillyapps.meantime.data.repository.AppRepository
import com.sillyapps.meantime.ui.SingleLiveEvent
import kotlinx.coroutines.launch
import java.util.*

class SchemeViewModel @ViewModelInject constructor(private val repository: AppRepository): ViewModel() {

    val scheme = MutableLiveData<Scheme>()

    val templates: LiveData<MutableList<SchemeTemplate>> = scheme.map { it.schemeTemplates }

    val templateAdded = SingleLiveEvent<Void>()

    init {
        viewModelScope.launch {
            scheme.value = repository.getCurrentSchemeWithTemplates()
        }
    }

    fun notifyItemsSwapped(fromPosition: Int, toPosition: Int) {
        Collections.swap(templates.value!!, fromPosition, toPosition)
    }

    fun notifyItemRemoved(position: Int) {
        templates.value!!.removeAt(position)
    }

    fun notifyItemDisabled(position: Int) {
        templates.value!![position].templateInfo.disable()
    }

    fun addTemplate(templateId: Int) {
        viewModelScope.launch {
            templates.value!!.add(SchemeTemplate(SchemeTemplateInfo(templateId), repository.loadTemplate(templateId)!!))
            templateAdded.call()
        }
    }

    fun saveScheme() {
        viewModelScope.launch {
            scheme.value?.apply {
                orderList.clear()
                schemeTemplates.mapTo(orderList) { it.templateInfo }
                repository.updateCurrentScheme(this)
            }
        }
    }

}