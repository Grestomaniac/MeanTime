package com.sillyapps.meantime.ui.explorer

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.sillyapps.meantime.data.SimplifiedTemplate
import com.sillyapps.meantime.data.repository.AppRepository
import kotlinx.coroutines.launch

class TemplateExplorerViewModel @ViewModelInject constructor(private val repository: AppRepository): ViewModel() {

    val items = repository.observeAllTemplates()

    fun deleteTemplate(position: Int) {
        viewModelScope.launch {
            val template = items.value!![position]
            repository.deleteTemplate(template)
        }
    }

    fun selectDefaultTemplate(position: Int) {
        viewModelScope.launch {
            val templateId = items.value!![position].id
            repository.setNewDefaultTemplate(templateId)
        }
    }

    fun getSimplifiedTemplate(position: Int): SimplifiedTemplate {
        val template = items.value!![position]
        return template.simplify()
    }

}