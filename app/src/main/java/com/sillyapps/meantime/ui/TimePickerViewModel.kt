package com.sillyapps.meantime.ui

interface TimePickerViewModel {
    fun setTaskDuration(duration: Long)
    fun getTaskDuration(): Long
}