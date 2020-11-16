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
import com.sillyapps.meantime.ui.mainscreen.DayManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(private val templateDao: TemplateDao,
                                        private val schemeDao: SchemeDao,
                                        private val appPrefDao: ApplicationPreferencesDao) {

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

    suspend fun loadTemplate(templateId: Int): Template? {
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

    private suspend fun loadTemplate(getNextTemplate: Boolean = true): Template? {
        var templateId = appPrefDao.getDefaultTemplateId()

        getTemplateFromScheme(getNextTemplate)?.let { templateId = it }

        return templateDao.getTemplate(templateId)
    }

    private suspend fun getTemplateFromScheme(getNextTemplate: Boolean = true): Int? {
        var templateId = -1
        val scheme = getCurrentScheme()
        scheme?.let { _scheme ->
            if (_scheme.isActive) {
                if (getNextTemplate) {
                    _scheme.getNextTemplateId()?.let { templateId = it }
                }
                else {
                    templateId = _scheme.getCurrentTemplateId()
                }

                schemeDao.update(_scheme)
                return templateId
            }
        }
        return null
    }

    suspend fun getDay(request: DayManager.RequestType): Day? {
        when (request) {
            DayManager.RequestType.GET_NEXT -> {
                appPrefDao.setCurrentDay(Day.fromTemplate(loadTemplate()))
            }
            DayManager.RequestType.REFRESH -> {
                appPrefDao.setCurrentDay(Day.fromTemplate(loadTemplate(false)))
            }
            else -> {
                val day = appPrefDao.getDay()
                if (day == null) appPrefDao.setCurrentDay(Day.fromTemplate(loadTemplate()))
            }
        }

        return appPrefDao.getDay()
    }

}