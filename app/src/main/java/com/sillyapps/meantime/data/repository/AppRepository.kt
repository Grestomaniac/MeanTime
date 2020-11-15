package com.sillyapps.meantime.data.repository

import androidx.lifecycle.LiveData
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.data.Day
import com.sillyapps.meantime.data.Scheme
import com.sillyapps.meantime.data.SimplifiedTemplate
import com.sillyapps.meantime.data.Template
import com.sillyapps.meantime.data.local.ApplicationPreferencesDao
import com.sillyapps.meantime.data.local.SchemeDao
import com.sillyapps.meantime.data.local.TemplateDao
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(private val templateDao: TemplateDao,
                                        private val schemeDao: SchemeDao,
                                        private val appPrefDao: ApplicationPreferencesDao,
                                        private val ioDispatcher: CoroutineDispatcher) {

    private suspend fun findNewDefaultTemplate() {
        appPrefDao.setDefaultTemplateId(0)

        val newTemplate = templateDao.getFirstExistingTemplate()

        newTemplate?.let {
            appPrefDao.setDefaultTemplateId(it.id)
            templateDao.setTemplateDefault(it.id, true)
        }
    }

    suspend fun setNewDefaultTemplate(templateId: Int) {
        val lastDefaultTemplateId = appPrefDao.getDefaultTemplateId()
        if (lastDefaultTemplateId == templateId) return

        if (lastDefaultTemplateId != 0) {
            templateDao.setTemplateDefault(lastDefaultTemplateId, false)
        }
        templateDao.setTemplateDefault(templateId, true)

        appPrefDao.setDefaultTemplateId(templateId)
    }

    suspend fun insertTemplate(template: Template): Int {
        val defaultTemplateId = appPrefDao.getDefaultTemplateId()
        if (defaultTemplateId == template.id) template.chosenAsDefault = true

        val templateId = templateDao.insertTemplate(template).toInt()

        if (defaultTemplateId == 0) {
            findNewDefaultTemplate()
        }

        return templateId
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

    fun observeCurrentScheme(): LiveData<Scheme> {
        return schemeDao.observeScheme(AppConstants.DEFAULT_SCHEME_ID)
    }

    suspend fun updateCurrentScheme(scheme: Scheme) {
        schemeDao.update(scheme)
    }

    suspend fun getCurrentScheme(): Scheme? {
        val currentSchemeId = appPrefDao.getDefaultSchemeId()
        return schemeDao.getScheme(currentSchemeId)
    }

    suspend fun setCurrentScheme(schemeId: Int) {
        appPrefDao.setDefaultSchemeId(schemeId)
    }

    suspend fun addTemplateToCurrentScheme(simplifiedTemplate: SimplifiedTemplate) {
        val scheme = schemeDao.getScheme(AppConstants.DEFAULT_SCHEME_ID)!!
        scheme.orderList.add(simplifiedTemplate)
        schemeDao.update(scheme)
    }

    private suspend fun getNextTemplate(): Template? {
        var templateId = appPrefDao.getDefaultTemplateId()

        val scheme = getCurrentScheme()
        scheme?.let { _scheme ->
            if (_scheme.isActive) {
                _scheme.getNextTemplateId()?.let { templateId = it }
            }
            schemeDao.update(_scheme)
        }

        return templateDao.getTemplate(templateId)
    }

    suspend fun getCurrentDay(): Day? {
        val day = appPrefDao.getDay()

        if (day == null) {
            setNewDay()
        }

        return appPrefDao.getDay()
    }

    suspend fun setNewDay() {
        appPrefDao.setCurrentDay(Day.fromTemplate(getNextTemplate()))
    }

}