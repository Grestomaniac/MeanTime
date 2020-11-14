package com.sillyapps.meantime.data

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.ObservableInt
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sillyapps.meantime.BR

@Entity(tableName = "schemes_table")
class Scheme(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "DEFAULT",
    val orderList: MutableList<Int> = mutableListOf(),
    val currentTemplate: Int = 0,
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

}