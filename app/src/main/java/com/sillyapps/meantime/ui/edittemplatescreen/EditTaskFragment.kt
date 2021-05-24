package com.sillyapps.meantime.ui.edittemplatescreen

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.sillyapps.meantime.R
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.databinding.FragmentEditTaskBinding
import com.sillyapps.meantime.setupToolbar
import com.sillyapps.meantime.ui.TimePickerDialog
import com.sillyapps.meantime.utils.convertToMillis
import com.sillyapps.meantime.utils.showInfoToUser
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment for editing and creating the [Task]
 */
@AndroidEntryPoint
class EditTaskFragment : Fragment() {

    private val viewModel: EditTemplateViewModel by navGraphViewModels(R.id.edit_template_graph) {
        defaultViewModelProviderFactory
    }

    private lateinit var binding: FragmentEditTaskBinding

    private val ringtoneManagerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val ringToneUri = data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)

            ringToneUri?.let {
                viewModel.setTaskSound(it.toString()) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditTaskBinding.inflate(inflater, container, false)
        binding.task = viewModel.task.value
        binding.fragment = this

        setupToolbar(binding.toolbar)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this.viewLifecycleOwner

//        binding.melody.setOnClickListener { showRingtonePicker() }
    }

    fun validateData() {
//        viewModel.setTaskDuration(binding.timePicker.getDuration())

        when(viewModel.isTaskDataValid()) {
            Task.WhatIsWrong.NOTHING -> saveTask()
            Task.WhatIsWrong.NAME -> showInfoToUser(R.string.name_is_empty)
            Task.WhatIsWrong.DURATION -> showInfoToUser(R.string.duration_is_zero)
        }
    }

    private fun saveTask() {
        viewModel.addCreatedTask()
        findNavController().popBackStack()
    }

    fun openBreakDialog() {
        val dialog = BreakDialog(viewModel.task.value!!.taskBreak) { viewModel.setTaskBreak(it) }
        dialog.show(childFragmentManager, "BreakDialog")
    }

    fun openTimePicker() {
        val dialog = TimePickerDialog(viewModel.getTaskDuration()) { viewModel.setTaskDuration(it) }
        dialog.show(childFragmentManager, "TimePicker")
    }

    private fun showRingtonePicker() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.choose_task_sound))
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(viewModel.task.value!!.sound))

        ringtoneManagerLauncher.launch(intent)
    }

}