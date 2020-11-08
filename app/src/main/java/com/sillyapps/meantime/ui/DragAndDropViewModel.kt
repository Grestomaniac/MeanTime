package com.sillyapps.meantime.ui

interface DragAndDropViewModel {
    fun recalculateStartTimes(position: Int)

    fun notifyTasksSwapped(upperPosition: Int, bottomPosition: Int)

    fun notifyTaskSwiped(position: Int)
}