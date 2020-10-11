package com.sillyapps.meantime.ui.explorer

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.sillyapps.meantime.data.Template
import com.sillyapps.meantime.data.repository.AppRepository
import kotlinx.coroutines.launch

class TemplateExplorerViewModel @ViewModelInject constructor(repository: AppRepository): ViewModel() {

    val items: LiveData<List<Template>> = repository.observeAllTemplates()

}