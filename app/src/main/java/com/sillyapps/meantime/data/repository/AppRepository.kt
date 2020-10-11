package com.sillyapps.meantime.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sillyapps.meantime.data.Day
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.data.Template
import com.sillyapps.meantime.data.local.SchemeDao
import com.sillyapps.meantime.data.local.TemplateDao
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(private val templateDao: TemplateDao,
                                        private val schemeDao: SchemeDao,
                                        private val ioDispatcher: CoroutineDispatcher) {

    private val DEFAULT_TEMPLATE_ID = 1

    private val _currentDay: MutableLiveData<Day> = MutableLiveData()
    val currentDay: LiveData<Day>
        get() = _currentDay

    init {
        /*CoroutineScope(ioDispatcher).launch {
            _currentDay.postValue(Day(getDefaultTemplateList()))
        }*/
    }

    fun startNewDay() {
        _currentDay.value?.startNewDay()
    }

    fun stopDay() {
        _currentDay.value?.stopDay()
    }

    suspend fun getDefaultTemplateList(): List<Task>? {
        return getTemplate(DEFAULT_TEMPLATE_ID)!!.activities
    }

    suspend fun insertTemplate(template: Template) {
        templateDao.insertTemplate(template)
    }

    suspend fun getTemplate(templateId: Int): Template? {
        return templateDao.getTemplate(templateId)
    }

    suspend fun getAllTemplates(): List<Template> {
        return templateDao.getAllTemplates()
    }

    fun observeCurrentTemplate(): LiveData<Template> {
        val currentTemplate = templateDao.observeTemplate(1)
        if (currentTemplate.value == null) {
            CoroutineScope(ioDispatcher).launch { createNewTemplate() }
        }
        return currentTemplate
    }

    fun observeAllTemplates(): LiveData<List<Template>> {
        return templateDao.observeTemplates()
    }


    private suspend fun createNewTemplate() {
        val newTemplate = Template(1)
        insertTemplate(newTemplate)
    }

}