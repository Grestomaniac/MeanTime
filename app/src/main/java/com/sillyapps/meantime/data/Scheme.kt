package com.sillyapps.meantime.data

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sillyapps.meantime.BR

@Entity(tableName = "schemes_table")
class Scheme(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "DEFAULT",
    val orderList: MutableList<SimplifiedTemplate> = mutableListOf(),
    var currentTemplatePos: Int = 0,
    isActive: Boolean = false,
    repeat: Boolean = false
): BaseObservable() {

    @Bindable
    var isActive: Boolean = isActive
        set(value) {
            field = value
            notifyPropertyChanged(BR.isActive)
        }

    @Bindable
    var repeat: Boolean = repeat
        set(value) {
            field = value
            notifyPropertyChanged(BR.repeat)
        }

    fun getNextTemplateId(): Int? {
        if (currentTemplatePos == orderList.lastIndex) {
            resetScheme()

            if (!repeat) {
                isActive = false
                return null
            }
            return orderList[currentTemplatePos].id
        }
        if (currentTemplatePos != -1)
            orderList[currentTemplatePos].state = State.COMPLETED

        currentTemplatePos++

        return setTemplate()
    }

    private fun setTemplate(): Int? {
        orderList[currentTemplatePos].let {
            if (it.state == State.DISABLED) {
                return getNextTemplateId()
            }
            it.state = State.ACTIVE
            return orderList[currentTemplatePos].id
        }
    }

    fun resetScheme() {
        for (template in orderList) {
            template.state = State.WAITING
        }
        currentTemplatePos = 0
    }
}