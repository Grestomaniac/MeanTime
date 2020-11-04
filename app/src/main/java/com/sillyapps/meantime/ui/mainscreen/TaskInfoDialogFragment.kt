package com.sillyapps.meantime.ui.mainscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.sillyapps.meantime.databinding.DialogTaskInfoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskInfoDialogFragment: DialogFragment() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: DialogTaskInfoBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogTaskInfoBinding.inflate(inflater, container, false)

        binding.task = viewModel.task.value

        return binding.root
    }

}