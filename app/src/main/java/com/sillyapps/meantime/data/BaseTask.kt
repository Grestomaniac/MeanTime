package com.sillyapps.meantime.data

import android.graphics.drawable.Drawable
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.sillyapps.meantime.BR
import com.sillyapps.meantime.utils.formatString
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "base_task_table")
class BaseTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val formattedName: String = formatString(name),
    val iconResId: Int = -1,

    val defaultDuration: Long = 0L,
    val defaultBreakInterval: Task.Break = Task.Break()
): BaseObservable() {

    companion object{
        fun fromTask(task: Task, iconId: Int = -1): BaseTask {
            return BaseTask(0, task.name, formatString(task.name), iconId)
        }
    }
}

class EditableBaseTask(): BaseObservable() {
    var name: String = ""
    var iconResId: Int = -1
    var defaultDuration: Long = 0L
    var defaultBreakInterval: Task.Break = Task.Break()

    var iconDrawable: Drawable? = null

    fun validateData(): WhatIsWrong {
        if (name.isEmpty()) return WhatIsWrong.NAME
        return WhatIsWrong.NOTHING
    }

    fun fillData(baseTask: BaseTask) {
        name = baseTask.name
        iconResId = baseTask.iconResId
        defaultDuration = baseTask.defaultDuration
        defaultBreakInterval = baseTask.defaultBreakInterval
    }

    fun toBaseTask(id: Long): BaseTask {
        return BaseTask(id, name,
            iconResId = iconResId, defaultDuration = defaultDuration, defaultBreakInterval = defaultBreakInterval)
    }

    enum class WhatIsWrong {
        NOTHING, NAME
    }
}

data class AssembledBaseTask(
    @Embedded val baseTask: BaseTask,
    @Relation(parentColumn = "id", entityColumn = "baseTaskId")
    val goals: Goals,
    var drawable: Drawable? = null
)

data class SimpleBaseTask(val id: Long,
                          val name: String,
                          val formattedName: String,
                          var iconResId: Int)

