package com.sillyapps.meantime.ui.edittemplatescreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.sillyapps.meantime.R
import com.sillyapps.meantime.data.EditableTask
import com.sillyapps.meantime.databinding.FragmentEditTaskBinding
import com.sillyapps.meantime.ui.TimePickerFragment
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class EditTaskFragment : Fragment() {

    private val viewModel: EditTemplateViewModel by navGraphViewModels(R.id.edit_template_graph) {
        defaultViewModelProviderFactory
    }

    private lateinit var viewDataBinding: FragmentEditTaskBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = FragmentEditTaskBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        viewDataBinding.duration.setOnClickListener { showTimePickerDialog() }
        viewDataBinding.okFab.setOnClickListener { validateData() }

        viewModel.task.observe(viewLifecycleOwner, {
            Timber.d("Task had been changed")
        })
    }

    private fun showTimePickerDialog() {
        TimePickerFragment().show(parentFragmentManager, "Pick time")
    }

    private fun validateData() {
        when(viewModel.isTaskDataValid()) {
            EditableTask.WhatIsWrong.NOTHING -> saveTask()
            EditableTask.WhatIsWrong.NAME -> showInfoToUser(R.string.name_is_empty)
            EditableTask.WhatIsWrong.DURATION -> showInfoToUser(R.string.duration_is_zero)
        }
    }

    private fun saveTask() {
        viewModel.addCreatedTask()
        findNavController().popBackStack()
    }

    private fun showInfoToUser(messageId: Int) {
        val message = getString(messageId)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}