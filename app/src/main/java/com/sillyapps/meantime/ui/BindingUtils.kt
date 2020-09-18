package com.sillyapps.meantime.ui

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.sillyapps.meantime.R
import kotlin.math.roundToInt

@BindingAdapter("setTextId")
fun TextView.setTextId(id: Int) {
    text = context.getString(R.string.ids, id)
}