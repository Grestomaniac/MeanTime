package com.sillyapps.meantime.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sillyapps.meantime.data.ApplicationPreferences
import com.sillyapps.meantime.data.Day

@Dao
interface ApplicationPreferencesDao {

    @Query("select * from app_pref_table where id = 1")
    fun observeApplicationPref(): LiveData<ApplicationPreferences>

    @Update
    suspend fun update(appPref: ApplicationPreferences)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appPref: ApplicationPreferences): Long

    @Query("select * from app_pref_table where id = 1")
    suspend fun getApplicationPref(): ApplicationPreferences?

    @Query("update app_pref_table set defaultTemplateId = :templateId where id = 1")
    suspend fun setDefaultTemplateId(templateId: Int)

    @Query("select defaultTemplateId from app_pref_table where id = 1")
    suspend fun getDefaultTemplateId(): Int

    @Query("update app_pref_table set defaultSchemeId = :schemeId where id = 1")
    suspend fun setDefaultSchemeId(schemeId: Int)

    @Query("select defaultSchemeId from app_pref_table where id = 1")
    suspend fun getDefaultSchemeId(): Int

    @Query("select day from app_pref_table where id = 1")
    suspend fun getDay(): Day?

    @Query("update app_pref_table set day = :day where id = 1")
    suspend fun setCurrentDay(day: Day?)
}