package com.sillyapps.meantime.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.sillyapps.meantime.databinding.DialogTimePickerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TimePickerDialog(private val duration: Long, private val onObtainResult: OnObtainResultListener): DialogFragment() {

    private lateinit var binding: DialogTimePickerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogTimePickerBinding.inflate(inflater, container, false)
        setupTimePicker()

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.okButton.setOnClickListener { setDuration() }
        binding.cancelButton.setOnClickListener { dismiss() }
    }

    private fun setDuration() {
        binding.apply {
            onObtainResult.obtainResult(binding.timePicker.getDuration())
        }
        dismiss()
    }

    private fun setupTimePicker() {
        binding.timePicker.setDuration(duration)
    }

    fun interface OnObtainResultListener {
        fun obtainResult(value: Long)
    }

}