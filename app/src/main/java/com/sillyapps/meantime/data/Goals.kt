package com.sillyapps.meantime.data

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sillyapps.meantime.BR
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

@Entity
class Goals(
    @PrimaryKey(autoGenerate = true) val goalId: Long = 0,
    val baseTaskId: Long,

    val tags: MutableMap<String, MutableList<Goal>> = HashMap()
) {
    fun removeGoal(goal: Goal) {
        if (!tags.containsKey(goal.tag)) return

        tags[goal.tag]!!.remove(goal)

        if (tags[goal.tag]!!.isEmpty()) tags.remove(goal.tag)
    }

    fun addGoal(goal: Goal) {
        if (!tags.containsKey(goal.tag)) return

        tags[goal.tag]!!.remove(goal)

        if (tags[goal.tag]!!.isEmpty()) tags.remove(goal.tag)
    }
}

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