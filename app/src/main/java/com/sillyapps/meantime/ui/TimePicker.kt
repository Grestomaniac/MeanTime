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
                timePickerHours.getTime(),
                timePickerMinutes.getTime(),
                timePickerSeconds.getTime()
            )
        }
    }

    private fun setupTimePickers() {
        binding.apply {
            timePickerHours.nextView = timePickerMinutes
            timePickerMinutes.nextView = timePickerSeconds

            timePickerMinutes.previousView = timePickerHours
            timePickerSeconds.previousView = timePickerMinutes

            timePickerSeconds.setAsLastPicker()
        }
    }

    fun fillTimePicker(task: Task) {
        binding.task = task
        val durationInMillis = task.editableDuration
        Timber.d("Duration is ${convertMillisToStringFormat(durationInMillis)}")

        binding.apply {
            val overallSeconds = (durationInMillis / 1000).toInt()
            timePickerSeconds.setTime(overallSeconds % 60)

            val overallMinutes = overallSeconds / 60
            timePickerMinutes.setTime(overallMinutes % 60)

            val overallHours = overallMinutes / 60
            timePickerHours.setTime(overallHours % 24)
        }
        setupTimePickers()
    }
}

@BindingAdapter("task")
fun TimePicker.setTask(task: Task) {
    fillTimePicker(task)
}