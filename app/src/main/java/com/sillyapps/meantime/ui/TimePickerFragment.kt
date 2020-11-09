package com.sillyapps.meantime.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import androidx.navigation.navGraphViewModels
import com.sillyapps.meantime.App
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.R
import com.sillyapps.meantime.convertToMillis
import com.sillyapps.meantime.databinding.DialogTimePickerBinding
import com.sillyapps.meantime.ui.edittemplatescreen.EditTemplateViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class TimePickerFragment: DialogFragment() {

    private val viewModel: EditTemplateViewModel by navGraphViewModels(R.id.edit_template_graph) {
        defaultViewModelProviderFactory
    }

    private lateinit var binding: DialogTimePickerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogTimePickerBinding.inflate(inflater, container, false)
        setupTimePickers()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.okButton.setOnClickListener { setDuration() }
        binding.uncertainButton.setOnClickListener { setUncertainDuration() }
    }

    private fun setDuration() {
        binding.apply {
            viewModel.setTaskDuration(convertToMillis(
                timePickerHours.value,
                timePickerMinutes.value,
                timePickerSeconds.value
            ))
        }
        dismiss()
    }

    private fun setUncertainDuration() {
        viewModel.setTaskDuration(AppConstants.UNCERTAIN)
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
        val durationInMillis = viewModel.task.value!!.duration

        if (durationInMillis == AppConstants.UNCERTAIN) {
            return
        }

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