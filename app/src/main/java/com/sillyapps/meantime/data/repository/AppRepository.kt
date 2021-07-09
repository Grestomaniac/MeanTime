package com.sillyapps.meantime.data.repository

import androidx.lifecycle.LiveData
import com.sillyapps.meantime.data.*
import com.sillyapps.meantime.data.local.ApplicationPreferencesDao
import com.sillyapps.meantime.data.local.SchemeDao
import com.sillyapps.meantime.data.local.BaseTaskDao
import com.sillyapps.meantime.data.local.TemplateDao
import com.sillyapps.meantime.ui.mainscreen.DayManager
import com.sillyapps.meantime.utils.formatString
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(private val templateDao: TemplateDao,
                                        private val schemeDao: SchemeDao,
                                        private val appPrefDao: ApplicationPreferencesDao,
                                        private val baseTaskDao: BaseTaskDao) {

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

    suspend fun deleteTemplate(template: Template) {

        val templateWasDefault = template.chosenAsDefault

        templateDao.deleteTemplate(template)

        // TODO Possible race condition
        if (templateWasDefault) findNewDefaultTemplate()
    }

    fun observeAllTemplates(): LiveData<List<Template>> {
        return templateDao.observeTemplates()
    }

    suspend fun updateCurrentScheme(scheme: Scheme) {
        schemeDao.update(scheme)
    }

    suspend fun getCurrentSchemeWithTemplates(): Scheme? {
        return getCurrentScheme()?.apply {
            for (schemeInfo in orderList) {
                val template = loadTemplate(schemeInfo.id)

                // Template is deleted
                if (template == null) {
                    orderList.remove(schemeInfo)
                    continue
                }
                schemeTemplates.add(SchemeTemplate(schemeInfo, template))
            }
        }
    }

    private suspend fun getCurrentScheme(): Scheme? {
        val currentSchemeId = appPrefDao.getDefaultSchemeId()
        return schemeDao.getScheme(currentSchemeId)
    }

    private suspend fun loadTemplate(getNextTemplate: Boolean = true): Template? {
        Timber.d("App pref dao is null = ${appPrefDao.getApplicationPref() == null}")
        var templateId = appPrefDao.getDefaultTemplateId()
        getTemplateFromScheme(getNextTemplate)?.let { templateId = it }

        return templateDao.getTemplate(templateId)
    }

    private suspend fun getTemplateFromScheme(getNextTemplate: Boolean = true): Int? {
        var templateId = -1

        getCurrentScheme()?.let { scheme ->
            if (scheme.isActive) {
                if (getNextTemplate) {
                    scheme.getNextTemplateId()?.let { templateId = it }
                }
                else {
                    templateId = scheme.getCurrentTemplateId()
                }

                schemeDao.update(scheme)
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

    suspend fun saveDay(day: Day?) {
        appPrefDao.setCurrentDay(day)
    }

    fun observeAllTaskGoals(): LiveData<List<BaseTask>> {
        return baseTaskDao.observeAllTaskGoals()
    }

    suspend fun getTaskGoals(taskGoalsId: Int): BaseTask? {
        return baseTaskDao.getTaskGoals(taskGoalsId)
    }

    suspend fun updateGoals(baseTask: BaseTask) {
        baseTaskDao.updateGoals(baseTask)
    }

    suspend fun getTaskGoalIdByName(taskName: String): Int {
        var taskGoalsId = baseTaskDao.getTaskGoalsIdByName(formatString(taskName))
        if (taskGoalsId == null) {
            taskGoalsId = baseTaskDao.insertTaskGoals(BaseTask(0, taskName)).toInt()
        }

        return taskGoalsId
    }

}