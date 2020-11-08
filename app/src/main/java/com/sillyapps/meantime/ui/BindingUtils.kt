package com.sillyapps.meantime.ui

import android.content.res.ColorStateList
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.R
import com.sillyapps.meantime.convertMillisToStringFormat
import com.sillyapps.meantime.convertMillisToStringFormatWithSeconds
import com.sillyapps.meantime.data.Task

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
fun ConstraintLayout.updateState(state: Task.State) {
    val backgroundResource =
        when (state) {
            Task.State.WAITING -> R.drawable.item_waiting
            Task.State.COMPLETED -> R.drawable.item_completed
            Task.State.DISABLED -> R.drawable.item_disabled
            Task.State.ACTIVE -> R.drawable.item_active
        }

    setBackgroundResource(backgroundResource)
}

@BindingAdapter("time")
fun TextView.setTime(time: Long) {
    text = convertMillisToStringFormat(time)
}

@BindingAdapter("timeWithSeconds")
fun TextView.setTimeWithSeconds(time: Long) {
    text = convertMillisToStringFormatWithSeconds(time)
}

@BindingAdapter("soundText")
fun TextView.setSoundString(uriPath: String) {
    if (uriPath == AppConstants.DEFAULT_RINGTONE)
        text = context.getString(R.string.default_ringtone)

    val cursor = context.contentResolver.query(Uri.parse(uriPath), null, null, null, null)
    cursor?.moveToFirst()
    text = cursor?.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
    cursor?.close()
}

@BindingAdapter("warningColor")
fun ImageView.setWarningColor(colorId: Int) {
    val resolvedColor = ContextCompat.getColor(context, colorId)
    imageTintList = ColorStateList.valueOf(resolvedColor)
}

@BindingAdapter("warningColor")
fun TextView.setWarningColor(colorId: Int) {
    val resolvedColor = ContextCompat.getColor(context, colorId)
    setTextColor(resolvedColor)
}