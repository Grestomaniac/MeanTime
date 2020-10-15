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
    suspend fun getApplicationPref(): ApplicationPreferences

    @Query("update app_pref_table set defaultTemplateId = :templateId where id = 1")
    suspend fun setDefaultTemplate(templateId: Int)

    @Query("update app_pref_table set day = :day where id = 1")
    suspend fun setCurrentDay(day: Day?)
}