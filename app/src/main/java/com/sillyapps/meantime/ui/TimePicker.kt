package com.sillyapps.meantime.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.databinding.TimePickerBinding
import com.sillyapps.meantime.utils.*
import timber.log.Timber

class TimePicker @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttrs: Int = 0): ConstraintLayout(context, attrs, defStyleAttrs) {

    private val binding: TimePickerBinding = TimePickerBinding.inflate(LayoutInflater.from(context), this, true)

    fun getDuration(): Long {
        binding.apply {
            return convertToMillis(
                hourField.getTime(),
                minuteField.getTime()
            )
        }
    }

    fun setDuration(durationInMillis: Long) {
        binding.apply {
            val overallSeconds = (durationInMillis / 1000).toInt()

            val overallMinutes = overallSeconds / 60
            minuteField.setTime(overallMinutes % 60)

            val overallHours = overallMinutes / 60
            hourField.setTime(overallHours % 24)
        }
    }
}