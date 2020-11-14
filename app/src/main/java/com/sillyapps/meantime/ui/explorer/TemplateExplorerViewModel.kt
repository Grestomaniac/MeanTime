package com.sillyapps.meantime.ui.explorer

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.sillyapps.meantime.data.Template
import com.sillyapps.meantime.ui.schemescreen.TemplatesManager
import kotlinx.coroutines.launch

class TemplateExplorerViewModel @ViewModelInject constructor(private val templatesManager: TemplatesManager): ViewModel() {

    val items: MutableLiveData<List<Template>> = MutableLiveData()

    init {
        viewModelScope.launch {
            templatesManager.loadTemplates()
            items.value = templatesManager.allTemplates
        }
    }

    fun deleteTemplate(position: Int) {
        viewModelScope.launch { templatesManager.deleteTemplate(position) }
    }

    fun selectDefaultTemplate(templateId: Int) {
        viewModelScope.launch { templatesManager.setNewDefaultTemplate(templateId) }
    }

}