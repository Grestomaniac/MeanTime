package com.sillyapps.meantime.ui

import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableBoolean
import com.sillyapps.meantime.R
import timber.log.Timber
import kotlin.math.roundToInt

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