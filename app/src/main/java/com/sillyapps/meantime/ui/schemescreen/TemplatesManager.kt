package com.sillyapps.meantime.ui.schemescreen

import androidx.lifecycle.LiveData
import com.sillyapps.meantime.data.Scheme
import com.sillyapps.meantime.data.Template
import com.sillyapps.meantime.data.repository.AppRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplatesManager @Inject constructor(private val repository: AppRepository) {

    var allTemplates: List<Template>? = null

    suspend fun loadTemplates() {
        allTemplates = repository.getAllTemplates()
    }

    suspend fun getCurrentScheme(): Scheme? {
        return repository.getCurrentScheme()
    }

    suspend fun deleteTemplate(position: Int) {
        repository.deleteTemplate(allTemplates!![position])
    }

    suspend fun setNewDefaultTemplate(templateId: Int) {
        repository.setNewDefaultTemplate(templateId)
    }
}