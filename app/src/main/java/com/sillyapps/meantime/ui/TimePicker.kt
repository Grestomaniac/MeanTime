package com.sillyapps.meantime.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.NumberPicker
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.databinding.TimePickerBinding
import com.sillyapps.meantime.utils.convertMillisToStringFormat
import com.sillyapps.meantime.utils.convertToMillis
import timber.log.Timber

class TimePicker @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttrs: Int = 0): ConstraintLayout(context, attrs, defStyleAttrs) {

    private val binding: TimePickerBinding = TimePickerBinding.inflate(LayoutInflater.from(context), this, true)

    override fun onFinishInflate() {
        super.onFinishInflate()

        setupTimePickers()
    }

    fun getDuration(): Long {
        binding.apply {
            return convertToMillis(
                timePickerHours.value,
                timePickerMinutes.value,
                timePickerSeconds.value
            )
        }
    }

    private fun setupTimePickers() {
        binding.apply {
            timePickerHours.minValue = 0
            timePickerHours.maxValue = 23

            timePickerMinutes.minValue = 0
            timePickerMinutes.maxValue = 59

            timePickerSeconds.maxValue = 0
            timePickerSeconds.maxValue = 59

            timePickerMinutes.setFormatter(timeFormatter)
            timePickerSeconds.setFormatter(timeFormatter)
        }
    }

    fun fillTimePicker(task: Task) {
        binding.task = task
        val durationInMillis = task.editableDuration
        Timber.d("Duration is ${convertMillisToStringFormat(durationInMillis)}")

        binding.apply {
            val overallSeconds = (durationInMillis / 1000).toInt()
            timePickerSeconds.value = overallSeconds % 60

            val overallMinutes = overallSeconds / 60
            timePickerMinutes.value = overallMinutes % 60

            val overallHours = overallMinutes / 60
            timePickerHours.value = overallHours % 24
        }
    }

    private val timeFormatter = NumberPicker.Formatter {
        if (it > 9) it.toString()
        else "0$it"
    }
}

@BindingAdapter("task")
fun TimePicker.setTask(task: Task) {
    fillTimePicker(task)
}