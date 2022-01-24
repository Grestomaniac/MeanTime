package com.sillyapps.meantime.ui.edittemplatescreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.maltaisn.icondialog.IconDialog
import com.maltaisn.icondialog.IconDialogSettings
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.pack.IconPack
import com.sillyapps.meantime.R
import com.sillyapps.meantime.data.BaseTask
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.databinding.FragmentEditBaseTaskBinding
import com.sillyapps.meantime.databinding.FragmentEditTaskBinding
import com.sillyapps.meantime.setupToolbar
import com.sillyapps.meantime.ui.TimePickerDialog
import com.sillyapps.meantime.ui.edittemplatescreen.taskchooser.TaskChooserDialog
import com.sillyapps.meantime.utils.showInfoToUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditBaseTaskFragment() : Fragment(), IconDialog.Callback {
    private val viewModel: EditBaseTaskViewModel by viewModels()

    private lateinit var binding: FragmentEditBaseTaskBinding

    override val iconDialogIconPack: IconPack
        get() = viewModel.iconPack

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditBaseTaskBinding.inflate(inflater, container, false)
        binding.baseTask = viewModel.baseTask.value

        setupToolbar(binding.toolbar)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onIconDialogIconsSelected(dialog: IconDialog, icons: List<Icon>) {
        viewModel.setTaskIcon(icons.first().id)
    }

    fun validateData() {
//        viewModel.setTaskDuration(binding.timePicker.getDuration())

        when(viewModel.isDataValid()) {
            BaseTask.WhatIsWrong.NOTHING -> saveTask()
            BaseTask.WhatIsWrong.NAME -> showInfoToUser(R.string.name_is_empty)
        }
    }

    private fun saveTask() {
        viewModel.save()
        findNavController().popBackStack()
    }

    fun openBreakDialog() {
        val dialog = BreakDialog(viewModel.baseTask.value!!.defaultBreakInterval) { viewModel.setTaskBreak(it) }
        dialog.show(childFragmentManager, "BreakDialog")
    }

    fun openTimePicker() {
        val dialog = TimePickerDialog(viewModel.getDuration()) { viewModel.setDuration(it) }
        dialog.show(childFragmentManager, "TimePicker")
    }

    fun openIconDialog() {
        val iconDialog = parentFragmentManager.findFragmentByTag(EditTaskFragment.ICON_DIALOG_TAG) as IconDialog?
            ?: IconDialog.newInstance(IconDialogSettings())
        iconDialog.show(childFragmentManager, EditTaskFragment.ICON_DIALOG_TAG)
    }
}