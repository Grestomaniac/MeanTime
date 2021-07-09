package com.sillyapps.meantime.ui.edittemplatescreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.sillyapps.meantime.R
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.databinding.DialogBreakBinding
import com.sillyapps.meantime.databinding.DialogTimePickerBinding
import com.sillyapps.meantime.ui.TimePickerDialog
import com.sillyapps.meantime.utils.showInfoToUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BreakDialog(taskBreak: Task.Break, private val onObtainResultListener: OnObtainResultListener): DialogFragment() {

    private var taskBreak: Task.Break = taskBreak.copy()
    private lateinit var binding: DialogBreakBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogBreakBinding.inflate(inflater, container, false)
        binding.taskBreak = taskBreak
        binding.fragment = this

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
    }

    fun pickInterval() {
        val dialog = TimePickerDialog(taskBreak.breakInterval) { taskBreak.breakInterval = it }
        dialog.show(childFragmentManager, "TimePicker")
    }

    fun pickDuration() {
        val dialog = TimePickerDialog(taskBreak.breakDuration) { taskBreak.breakDuration = it }
        dialog.show(childFragmentManager, "TimePicker")
    }

    fun onCancelButtonClick() {
        dismiss()
    }

    fun onDisableButtonClick() {
        taskBreak.hasBreak = false
        onObtainResultListener.obtainResult(taskBreak)
        dismiss()
    }

    fun onOkButtonClick() {
        if (dataIsInvalid()) return

        taskBreak.hasBreak = true
        onObtainResultListener.obtainResult(taskBreak)
        dismiss()
    }

    private fun dataIsInvalid(): Boolean {
        if (taskBreak.breakInterval == 0L) {
            showInfoToUser(R.string.invalid_break_interval)
            return true
        }
        if (taskBreak.breakDuration == 0L) {
            showInfoToUser(R.string.invalid_break_duration)
            return true
        }
        return false
    }

    fun interface OnObtainResultListener {
        fun obtainResult(taskBreak: Task.Break)
    }
}