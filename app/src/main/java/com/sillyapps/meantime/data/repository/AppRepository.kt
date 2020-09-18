package com.sillyapps.meantime.data.repository

import androidx.lifecycle.LiveData
import com.sillyapps.meantime.data.Template
import com.sillyapps.meantime.data.local.SchemeDao
import com.sillyapps.meantime.data.local.TemplateDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(private val templateDao: TemplateDao,
                                        private val schemeDao: SchemeDao,
                                        private val ioDispatcher: CoroutineScope) {

    suspend fun insertTemplate(template: Template) {
        templateDao.insertTemplate(template)
    }

    fun getCurrentTemplate(): LiveData<Template> {
        val currentTemplate = templateDao.observeTemplate(1)
        if (currentTemplate.value == null) {
            val newTemplate = Template(1)
            ioDispatcher.launch { insertTemplate(newTemplate) }
        }
        return currentTemplate
    }


}