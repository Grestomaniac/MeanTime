package com.sillyapps.meantime.data

import android.os.Parcel
import android.os.Parcelable
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.BR

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
    fun simplify(): SimplifiedTemplate {
        return SimplifiedTemplate(id, name)
    }
}

class SimplifiedTemplate(
    val id: Int,
    val name: String,
    state: State = State.WAITING): BaseObservable(), Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!
    )

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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SimplifiedTemplate> {
        override fun createFromParcel(parcel: Parcel): SimplifiedTemplate {
            return SimplifiedTemplate(parcel)
        }

        override fun newArray(size: Int): Array<SimplifiedTemplate?> {
            return arrayOfNulls(size)
        }
    }
}