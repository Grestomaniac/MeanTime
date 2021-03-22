package com.sillyapps.meantime.ui.schemescreen

import androidx.lifecycle.*
import com.sillyapps.meantime.data.Scheme
import com.sillyapps.meantime.data.SchemeTemplate
import com.sillyapps.meantime.data.SchemeTemplateInfo
import com.sillyapps.meantime.data.Template
import com.sillyapps.meantime.data.repository.AppRepository
import com.sillyapps.meantime.ui.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SchemeViewModel @Inject constructor(private val repository: AppRepository): ViewModel() {

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