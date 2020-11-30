package com.sillyapps.meantime.ui.mainscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.convertToMillis
import com.sillyapps.meantime.databinding.DialogTaskInfoBinding
import com.sillyapps.meantime.databinding.DialogTemporalTaskBinding
import com.sillyapps.meantime.ui.TimePickerFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TemporalTaskDialogFragment: DialogFragment() {

    private val viewModel: MainViewModel by viewModels( ownerProducer = { requireParentFragment() } )

    private lateinit var binding: DialogTemporalTaskBinding

    private val timeFormatter = NumberPicker.Formatter {
        if (it > 9) it.toString()
        else "0$it"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogTemporalTaskBinding.inflate(inflater, container, false)
        binding.task = viewModel.task.value

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        setupTimePickers()
        binding.okButton.setOnClickListener { saveTask() }
    }

    private fun saveTask() {
        val taskDuration = binding.run {
            convertToMillis(
                timePickerHours.value,
                timePickerMinutes.value,
                timePickerSeconds.value
            )
        }
        viewModel.addTemporalTask(taskDuration)
        dismiss()
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
        setupTimePickerValues()
    }

    private fun setupTimePickerValues() {
        val durationInMillis = viewModel.getTaskDuration()

        binding.apply {
            val overallSeconds = (durationInMillis / 1000).toInt()
            timePickerSeconds.value = overallSeconds % 60

            val overallMinutes = overallSeconds / 60
            timePickerMinutes.value = overallMinutes % 60

            val overallHours = overallMinutes / 60
            timePickerHours.value = overallHours % 24
        }
    }



    override fun onDestroy() {
        viewModel.onTaskDialogClosed()
        super.onDestroy()
    }

}