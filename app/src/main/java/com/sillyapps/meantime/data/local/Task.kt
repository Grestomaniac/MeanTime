package com.sillyapps.meantime.data.local

data class Task(
    val name: String,
    val duration: Int,
    val soundOn: Boolean,
    val sound: String
)