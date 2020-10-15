package com.sillyapps.meantime.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sillyapps.meantime.data.ApplicationPreferences
import com.sillyapps.meantime.data.Day
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.data.Template
import com.sillyapps.meantime.data.local.ApplicationPreferencesDao
import com.sillyapps.meantime.data.local.SchemeDao
import com.sillyapps.meantime.data.local.TemplateDao
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(private val templateDao: TemplateDao,
                                        private val schemeDao: SchemeDao,
                                        private val appPrefDao: ApplicationPreferencesDao,
                                        private val ioDispatcher: CoroutineDispatcher) {

    private val _currentDay: MutableLiveData<Day> = MutableLiveData()
    val currentDay: LiveData<Day>
        get() = _currentDay

    private val appPref = appPrefDao.observeApplicationPref()

    init {
        appPref.observeForever {}
    }

    private suspend fun findNewDefaultTemplate() {
        appPrefDao.setDefaultTemplate(0)

        val newTemplate = templateDao.getFirstExistingTemplate()

        newTemplate?.let {
            appPrefDao.setDefaultTemplate(it.id)
            templateDao.setTemplateDefault(it.id, true)
        }
    }

    suspend fun updateAppPref(templateId: Int) {
        val lastDefaultTemplateId = appPref.value!!.defaultTemplateId
        if (lastDefaultTemplateId == templateId) return

        if (lastDefaultTemplateId != 0) {
            templateDao.setTemplateDefault(lastDefaultTemplateId, false)
        }
        templateDao.setTemplateDefault(templateId, true)

        appPrefDao.setDefaultTemplate(templateId)
    }

    suspend fun insertTemplate(template: Template): Long {
        return templateDao.insertTemplate(template)
    }

    suspend fun getTemplate(templateId: Int): Template? {
        return templateDao.getTemplate(templateId)
    }

    suspend fun getAllTemplates(): List<Template> {
        return templateDao.getAllTemplates()
    }

    suspend fun deleteTemplate(template: Template) {

        val templateWasDefault = template.chosenAsDefault

        templateDao.deleteTemplate(template)

        // TODO Possible race condition
        if (templateWasDefault) findNewDefaultTemplate()
    }

    fun observeAllTemplates(): LiveData<List<Template>> {
        return templateDao.observeTemplates()
    }

    suspend fun getNextTemplate(): Template? {
        // TODO get template from scheme

        return templateDao.getTemplate(appPref.value!!.defaultTemplateId)
    }

    suspend fun getCurrentDay(): Day? {
        val day = appPref.value?.day

        if (day == null) {
            appPrefDao.setCurrentDay(Day.fromTemplate(getNextTemplate()))
        }

        return appPref.value?.day
    }

}