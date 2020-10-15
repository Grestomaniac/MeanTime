package com.sillyapps.meantime.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates_table")
data class Template(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",

    @ColumnInfo(name = "chosen_as_default")
    var chosenAsDefault: Boolean = false,

    val activities: MutableList<Task> = mutableListOf()
)