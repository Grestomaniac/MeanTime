package com.sillyapps.meantime.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sillyapps.meantime.data.Template

@Dao
interface TemplateDao {
    @Query("select * from templates_table where id = :templateId")
    fun observeTemplate(templateId: Int): LiveData<Template>

    @Query("select * from templates_table")
    fun observeTemplates(): LiveData<List<Template>>

    @Query("select * from templates_table where id = :templateId")
    suspend fun getTemplate(templateId: Int): Template?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: Template): Long

    @Update
    suspend fun updateTemplate(template: Template)

    @Query("update templates_table set chosen_as_default = :isDefault where id = :templateId")
    suspend fun setTemplateDefault(templateId: Int, isDefault: Boolean)

    @Delete
    suspend fun deleteTemplate(template: Template)

    @Query("delete from templates_table")
    suspend fun deleteAll()

    @Query("select * from templates_table order by id asc limit 1")
    suspend fun getFirstExistingTemplate(): Template?
}