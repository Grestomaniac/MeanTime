package com.sillyapps.meantime.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_pref_table")
class ApplicationPreferences(
    @PrimaryKey
    val id: Int = 1,
    val defaultTemplateId: Int = 0,
    val defaultSchemeId: Int = 1,
    val day: Day? = null)