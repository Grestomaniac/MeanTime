package com.sillyapps.meantime.data.repository

import androidx.lifecycle.LiveData
import com.sillyapps.meantime.data.Result
import com.sillyapps.meantime.data.Template

interface BaseRepository {

    suspend fun getCurrentTemplate(): Result<Template>

    fun observeCurrentTemplate(): LiveData<Template>

    suspend fun updateCurrentTemplate(template: Template)

    suspend fun insertTemplate(template: Template): Int
}