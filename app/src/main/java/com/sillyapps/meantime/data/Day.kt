package com.sillyapps.meantime.data

data class Day(val tasks: List<Task>,
               var started: Boolean = false,
               var dayStartTime: Long = 0L,
               var currentTaskPos: Int = 0) {

    fun startNewDay() {
        dayStartTime = System.currentTimeMillis()
        started = true
        tasks.forEach { it.addCurrentDayOffset(dayStartTime) }
    }

    fun stopDay() {
        dayStartTime = 0L
        started = false
        tasks.forEach { it.updateUI() }
    }
}