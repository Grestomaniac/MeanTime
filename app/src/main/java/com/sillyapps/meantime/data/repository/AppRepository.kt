package com.sillyapps.meantime.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.sillyapps.meantime.data.*
import com.sillyapps.meantime.data.local.*
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
                Timber.d("Getting day")
                val day = appPrefDao.getDay()

                Timber.d("Day received")
                if (day == null) appPrefDao.setCurrentDay(Day.fromTemplate(loadTemplate()))
            }
        }

        return appPrefDao.getDay()
    }

    suspend fun saveDay(day: Day?) {
        appPrefDao.setCurrentDay(day)
    }

    fun observeAllBaseTasks(): LiveData<List<BaseTask>> {
        return baseTaskDao.observeAllBaseTasks()
    }

    fun observeAllBaseTasksSimple(): LiveData<List<SimpleBaseTask>> {
        return baseTaskDao.observeAllBaseTasksSimple()
    }

    suspend fun getBaseTask(baseTaskId: Long): BaseTask? {
        return baseTaskDao.getBaseTask(baseTaskId)
    }

    suspend fun updateBaseTask(baseTask: BaseTask) {
        baseTaskDao.updateBaseTask(baseTask)
    }

    suspend fun updateSimpleBaseTask(task: Task?, iconId: Int) {
        if (task == null) return

        val baseTaskId = baseTaskDao.getBaseTaskIdByName(formatString(task.name))
        if (baseTaskId == null) {
            baseTaskDao.insertBaseTask(BaseTask.fromTask(task, iconId))
            return
        }

        baseTaskDao.updateSimpleBaseTask(baseTaskId, iconId)
    }

    suspend fun getBaseTaskIdByName(taskName: String): Long {
        var baseTaskId = baseTaskDao.getBaseTaskIdByName(formatString(taskName))
        if (baseTaskId == null) {
            baseTaskId = baseTaskDao.insertBaseTask(BaseTask(0, taskName))
        }

        return baseTaskId
    }

}