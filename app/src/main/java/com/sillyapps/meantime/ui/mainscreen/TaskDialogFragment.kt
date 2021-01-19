package com.sillyapps.meantime.ui.mainscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.sillyapps.meantime.R
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.databinding.DialogAddTaskBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskDialogFragment: DialogFragment() {

    private val viewModel: MainViewModel by viewModels( ownerProducer = { requireParentFragment() } )

    private lateinit var binding: DialogAddTaskBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogAddTaskBinding.inflate(inflater, container, false)
        binding.task = viewModel.task.value

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        val nameAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
        binding.temporalTaskName.setAdapter(nameAdapter)

        binding.timePickerLayout.fillTimePicker(viewModel.task.value!!)
        binding.okButton.setOnClickListener { saveTask() }

        viewModel.taskGoals.observe(viewLifecycleOwner) {
            nameAdapter.clear()
            nameAdapter.addAll(it.map { taskGoals -> taskGoals.name })
        }
    }

    private fun saveTask() {
        val taskDuration = binding.timePickerLayout.getDuration()
        viewModel.setTaskDuration(taskDuration)

        when(viewModel.validateTaskData()) {
            Task.WhatIsWrong.NOTHING -> {
                viewModel.saveTask()
                dismiss()
            }
            Task.WhatIsWrong.NAME -> showInfoToUser(R.string.name_is_empty)
            Task.WhatIsWrong.DURATION -> showInfoToUser(R.string.duration_is_zero)
        }
    }

    private fun showInfoToUser(messageId: Int) {
        val message = getString(messageId)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        viewModel.onTaskDialogClosed()
        super.onDestroy()
    }

}