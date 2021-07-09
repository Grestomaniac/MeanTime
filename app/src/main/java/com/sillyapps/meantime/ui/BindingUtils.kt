package com.sillyapps.meantime.ui

import android.content.res.ColorStateList
import android.net.Uri
import android.provider.OpenableColumns
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.databinding.BindingAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.R
import com.sillyapps.meantime.utils.convertMillisToStringFormat
import com.sillyapps.meantime.utils.convertMillisToStringFormatWithSeconds
import com.sillyapps.meantime.data.State
import com.sillyapps.meantime.utils.getHoursAndMinutes
import com.sillyapps.meantime.utils.getVerboseTime
import timber.log.Timber

@BindingAdapter("isDefault")
fun View.setDefault(isDefault: Boolean) {
    val backgroundResource =
        if (!isDefault) {
            R.color.primaryColor
        }
        else {
            R.color.activeColor
        }

    setBackgroundResource(backgroundResource)
}

@BindingAdapter("taskSelected")
fun View.setTaskSelected(isSelected: Boolean) {
    val backgroundResource =
        if (isSelected) R.color.defaultBackground
        else R.color.itemColor
    setBackgroundResource(backgroundResource)
}

@BindingAdapter("state")
fun View.updateState(state: State) {
    val backgroundResource =
        when (state) {
            State.WAITING -> R.color.primaryColor
            State.COMPLETED -> R.color.completedColor
            State.DISABLED -> R.color.disabledColor
            State.ACTIVE -> R.color.activeColor
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

@BindingAdapter("usedTimes")
fun TextView.setUsedTimes(times: Int) {
    text = context.getString(R.string.used_times, times)
}

@BindingAdapter("app:verboseTime")
fun Button.setVerboseTime(timeInMillis: Long) {
    text = getVerboseTime(timeInMillis, context)
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
    foregroundTintList = if (isActive)
        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.disabledColor))
    else {
        null
    }
}

@BindingAdapter("isInteractive")
fun ImageButton.setInteractive(interactive: Boolean) {
    visibility = if (interactive) View.VISIBLE
                    else View.INVISIBLE
    isEnabled = interactive

}

@BindingAdapter("isDayRunning")
fun ImageButton.setDayRunning(dayRunning: Boolean) {
    val imageResource =
        if (dayRunning) R.drawable.ic_pause
        else R.drawable.ic_play

    setImageResource(imageResource)
}

@BindingAdapter("app:soundOn")
fun ImageView.setSoundOn(soundOn: Boolean) {
    if (soundOn) setImageResource(R.drawable.ic_sound_on)
    else setImageResource(R.drawable.ic_sound_off)
}

@BindingAdapter("app:vibrationOn")
fun ImageView.setVibrationOn(vibrationOn: Boolean) {
    if (vibrationOn) setImageResource(R.drawable.ic_vibration_on)
    else setImageResource(R.drawable.ic_vibrate_off)
}

@BindingAdapter("app:soundOn")
fun TextView.setSoundOn(soundOn: Boolean) {
    text =
        if (soundOn) context.getString(R.string.sound_on)
        else context.getString(R.string.sound_off)
}

@BindingAdapter("app:vibrationOn")
fun TextView.setVibrationOn(vibrationOn: Boolean) {
    text =
        if (vibrationOn) context.getString(R.string.vibration_on)
        else context.getString(R.string.vibration_off)
}