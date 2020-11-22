package com.sillyapps.meantime.ui.goalscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.sillyapps.meantime.databinding.DialogGoalBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoalDialogFragment: DialogFragment() {

    private val viewModel: GoalViewModel by viewModels(ownerProducer = { requireParentFragment() })

    private lateinit var binding: DialogGoalBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogGoalBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.saveGoalButton.setOnClickListener { saveGoal() }
    }

    private fun saveGoal() {
        viewModel.saveGoal()
        dismiss()
    }
}