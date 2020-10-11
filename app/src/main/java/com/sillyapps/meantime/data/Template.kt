package com.sillyapps.meantime.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates_table")
data class Template(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var name: String = "",
    var nextStartTime: Long = 0L,

    val activities: MutableList<Task> = mutableListOf()
)