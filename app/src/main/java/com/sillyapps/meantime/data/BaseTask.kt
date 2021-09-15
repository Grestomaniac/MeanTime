package com.sillyapps.meantime.data

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sillyapps.meantime.BR
import com.sillyapps.meantime.utils.formatString
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "goal_table")
class BaseTask(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val formattedName: String = formatString(name),
    var iconResId: Int = -1,

    val tagGroups: MutableList<Tag> = mutableListOf()
): BaseObservable() {

    fun removeGoal(goal: Goal) {
        val tagGroup = tagGroups.find { predicate -> predicate.name == goal.tag }
        tagGroup?.apply {
            contents.remove(goal)

            if (tagGroup.contents.isEmpty()) {
                tagGroups.remove(tagGroup)
            }
        }
    }

    fun addGoal(goal: Goal) {
        val tagGroup = tagGroups.find { it.name == goal.tag }
        if (tagGroup == null) {
            tagGroups.add(Tag(goal.tag, mutableListOf(goal)))
            return
        }
        tagGroup.contents.add(goal)
    }

    companion object{
        fun fromTask(task: Task, iconId: Int = -1): BaseTask {
            return BaseTask(0, task.name, formatString(task.name), iconId)
        }
    }
}

data class SimpleBaseTask(val id: Int,
                          val name: String,
                          val formattedName: String,
                          var iconResId: Int)

class Goal(
    name: String = "",
    tag: String = "",
    description: String = "",
    changedDate: String = "",
    isDefault: Boolean = false
): BaseObservable() {

    @Bindable
    var name: String = name
        set(value) {
            field = value
            notifyPropertyChanged(BR.name)
        }

    @Bindable
    var tag: String = tag
        set(value) {
            field = value
            notifyPropertyChanged(BR.tag)
        }

    @Bindable
    var description: String = description
        set(value) {
            field = value
            notifyPropertyChanged(BR.description)
        }

    @Bindable
    var changedDate: String = changedDate
        set(value) {
            field = value
            notifyPropertyChanged(BR.changedDate)
        }

    @Bindable
    var goalIsDefault: Boolean = isDefault
        set(value) {
            field = value
            notifyPropertyChanged(BR.goalIsDefault)
        }

    override fun equals(other: Any?): Boolean {
        val otherGoal = other as Goal
        return otherGoal.name == name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    fun saveGoal() {
        changedDate = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date())
        Timber.d("Saving goal, changedDate = ${changedDate}}")
    }

    fun copy(): Goal {
        return Goal(name, description, changedDate)
    }

    fun fillWith(goal: Goal) {
        name = goal.name
        description = goal.description
        changedDate = goal.changedDate
    }
}

class Tag(
    val name: String,
    val contents: MutableList<Goal>
)