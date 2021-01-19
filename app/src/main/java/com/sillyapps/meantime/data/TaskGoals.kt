package com.sillyapps.meantime.data

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.BR
import com.sillyapps.meantime.utils.formatString
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "goal_table")
class TaskGoals(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val formattedName: String = formatString(name),
    var defaultGoalPos: Int = AppConstants.NOT_ASSIGNED,

    @ColumnInfo(name = "active_goals")
    val activeGoals: MutableList<Goal> = mutableListOf(),

    @ColumnInfo(name = "completed_goals")
    val completedGoals: MutableList<Goal> = mutableListOf()
): BaseObservable() {
    fun selectDefaultGoal(position: Int) {
        when (defaultGoalPos) {
            AppConstants.NOT_ASSIGNED -> {
                defaultGoalPos = position
                activeGoals[position].goalIsDefault = true
            }
            position -> {
                unselectDefaultGoal()
            }
            else -> {
                activeGoals[defaultGoalPos].goalIsDefault = false
                activeGoals[position].goalIsDefault = true
                defaultGoalPos = position
            }
        }
    }

    fun unselectDefaultGoal() {
        activeGoals[defaultGoalPos].goalIsDefault = false
        defaultGoalPos = AppConstants.NOT_ASSIGNED
    }
}

class Goal(
    name: String = "",
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