package com.sillyapps.meantime.data

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sillyapps.meantime.BR

@Entity(tableName = "goal_table")
class TaskGoals(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val goals: MutableList<Goal> = mutableListOf()
): BaseObservable() {
}

class Goal(
    name: String = "",
    description: String = ""
): BaseObservable() {
    @Bindable
    var name: String = name
        set(value) {
            field = value
            notifyPropertyChanged(BR.name)
        }

    @Bindable
    var description: String = description
        set(value) {
            field = value
            notifyPropertyChanged(BR.description)
        }

    override fun equals(other: Any?): Boolean {
        val otherGoal = other as Goal
        return (otherGoal.name == name) and (otherGoal.description == description)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + description.hashCode()
        return result
    }

    fun copy(): Goal {
        return Goal(name, description)
    }

    fun fillWith(goal: Goal) {
        name = goal.name
        description = goal.description
    }
}