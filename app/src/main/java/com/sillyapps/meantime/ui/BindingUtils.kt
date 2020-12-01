package com.sillyapps.meantime.ui

import android.content.res.ColorStateList
import android.net.Uri
import android.provider.OpenableColumns
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.databinding.BindingAdapter
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.R
import com.sillyapps.meantime.convertMillisToStringFormat
import com.sillyapps.meantime.convertMillisToStringFormatWithSeconds
import com.sillyapps.meantime.data.State
import com.sillyapps.meantime.data.Task
import timber.log.Timber

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

@BindingAdapter("state")
fun ConstraintLayout.updateState(state: State) {
    val backgroundResource =
        when (state) {
            State.WAITING -> R.drawable.item_waiting
            State.COMPLETED -> R.drawable.item_completed
            State.DISABLED -> R.drawable.item_disabled
            State.ACTIVE -> R.drawable.item_active
        }

    setBackgroundResource(backgroundResource)
}

@BindingAdapter("time")
fun TextView.setTime(time: Long) {
    if (time == AppConstants.UNCERTAIN) {
        text = "??:??"
        return
    }
    text = convertMillisToStringFormat(time)
}

@BindingAdapter("timeWithSeconds")
fun TextView.setTimeWithSeconds(time: Long) {
    if (time == AppConstants.UNCERTAIN) {
        text = "??:??:??"
        return
    }
    text = convertMillisToStringFormatWithSeconds(time)
}

@BindingAdapter("soundText")
fun TextView.setSoundString(uriPath: String) {
    if (uriPath == AppConstants.DEFAULT_RINGTONE) {
        text = context.getString(R.string.default_ringtone)
        return
    }

    val cursor = context.contentResolver.query(Uri.parse(uriPath), null, null, null, null)
    cursor?.moveToFirst()
    text = cursor?.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
    cursor?.close()
}

@BindingAdapter("dialogBackground")
fun View.setDialogBackground(drawableId: Int) {
    background = ContextCompat.getDrawable(context, drawableId)
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

@BindingAdapter("interactive")
fun ConstraintLayout.setInteractive(isInteractive: Boolean) {
    isEnabled = isInteractive
    children.forEach { it.isEnabled = isInteractive }
}

@BindingAdapter("isActive")
fun View.setActive(isActive: Boolean) {
    if (isActive)
        foregroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorNotActive))
    else {
        foregroundTintList = null
    }
}

@BindingAdapter("isInteractive")
fun ImageButton.setInteractive(interactive: Boolean) {
    visibility = if (interactive) View.VISIBLE
                    else View.INVISIBLE
    isEnabled = interactive

}