package com.sillyapps.meantime.ui

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.sillyapps.meantime.R
import com.sillyapps.meantime.data.RunningTask

@BindingAdapter("isDefault")
fun ConstraintLayout.setDefault(isDefault: Boolean) {
    val backgroundResource =
        if (!isDefault) {
            R.drawable.item_waiting
        }
        else {
            R.drawable.item_active
        }

    setBackgroundResource(backgroundResource)
}

@BindingAdapter("taskState")
fun ConstraintLayout.updateState(state: RunningTask.State) {
    val backgroundResource =
        when (state) {
            RunningTask.State.WAITING -> R.drawable.item_waiting
            RunningTask.State.COMPLETED -> R.drawable.item_completed
            RunningTask.State.DISABLED -> R.drawable.item_disabled
            RunningTask.State.ACTIVE -> R.drawable.item_active
        }

    setBackgroundResource(backgroundResource)
}