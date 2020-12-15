package com.sillyapps.meantime.ui.mainscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.sillyapps.meantime.R
import com.sillyapps.meantime.databinding.DialogTaskInfoBinding
import com.sillyapps.meantime.ui.TimePickerFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskInfoDialogFragment: DialogFragment() {

    private val viewModel: MainViewModel by viewModels( ownerProducer = { requireParentFragment() } )

    private lateinit var binding: DialogTaskInfoBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogTaskInfoBinding.inflate(inflater, container, false)
        binding.task = viewModel.task.value

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.taskInfoDuration.setOnClickListener { editDuration() }
        binding.revertChangesButton.setOnClickListener { viewModel.onTaskRevertChanges() }
    }

    private fun editDuration() {
        TimePickerFragment(viewModel).show(parentFragmentManager, "Pick time")
    }

    override fun onDestroy() {
        viewModel.onTaskDialogClosed()
        super.onDestroy()
    }

}