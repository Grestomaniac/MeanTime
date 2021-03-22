package com.sillyapps.meantime.ui.explorer

import androidx.lifecycle.*
import com.sillyapps.meantime.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TemplateExplorerViewModel @Inject constructor(private val repository: AppRepository): ViewModel() {

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

    fun getTemplateId(position: Int): Int {
        val template = items.value!![position]
        return template.id
    }

}