package com.sillyapps.meantime.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schemes_table")
data class Scheme(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val orderList: List<Int>
)