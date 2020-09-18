package com.sillyapps.meantime.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sillyapps.meantime.data.local.Task

@Entity(tableName = "templates_table")
data class Template(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var name: String = "",

    val activities: List<Task> = emptyList()
)