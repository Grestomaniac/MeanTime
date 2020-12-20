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
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.sillyapps.meantime.R
import com.sillyapps.meantime.convertToMillis
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.databinding.FragmentEditTaskBinding
import com.sillyapps.meantime.ui.TimePickerFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditTaskFragment : DialogFragment() {

    val REQUEST_CODE = 5

    private val viewModel: EditTemplateViewModel by navGraphViewModels(R.id.edit_template_graph) {
        defaultViewModelProviderFactory
    }

    private lateinit var binding: FragmentEditTaskBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditTaskBinding.inflate(inflater, container, false)
        binding.task = viewModel.task.value

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = this.viewLifecycleOwner

        val nameAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
        binding.name.setAdapter(nameAdapter)

        binding.melody.setOnClickListener { showRingtonePicker() }
        binding.okFab.setOnClickListener { validateData() }

        viewModel.goalTasksNames.observe(viewLifecycleOwner) {
            nameAdapter.clear()
            nameAdapter.addAll(it)
        }
    }

    private fun validateData() {
        viewModel.setTaskDuration(binding.timePickerLayout.getDuration())

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

    private fun showInfoToUser(messageId: Int) {
        val message = getString(messageId)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showRingtonePicker() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.choose_task_sound))
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(viewModel.task.value!!.sound))
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                val ringToneUri = data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)

                ringToneUri?.let {
                    viewModel.setTaskSound(it.toString()) }
            }
        }
    }

}