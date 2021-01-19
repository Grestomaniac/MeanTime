package com.sillyapps.meantime.data

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.BR
import com.sillyapps.meantime.utils.convertMillisToStringFormat

@Entity(tableName = "templates_table")
data class Template(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",

    @ColumnInfo(name = "chosen_as_default")
    var chosenAsDefault: Boolean = false,

    val activities: MutableList<Task> = mutableListOf(),

    val alarmDuration: Long = AppConstants.ALARM_DEFAULT_DURATION
) {
    fun getDuration(): String {
        return convertMillisToStringFormat(activities.last().getNextStartTime())
    }

    fun getTaskCount(): String {
        return activities.size.toString()
    }
}

class SchemeTemplateInfo(
    val id: Int,
    state: State = State.WAITING): BaseObservable() {

    @Bindable
    var state = state
        set(value) {
            field = value
            notifyPropertyChanged(BR.state)
        }

    fun disable() {
        state = when (state) {
            State.DISABLED -> State.WAITING
            State.WAITING -> State.DISABLED
            else -> return
        }
    }

    fun start() {
        state = State.ACTIVE
    }

    fun complete() {
        state = State.ACTIVE
    }

    fun reset() {
        state = State.WAITING
    }
}