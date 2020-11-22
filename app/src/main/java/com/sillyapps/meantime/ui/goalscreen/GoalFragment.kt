package com.sillyapps.meantime.ui.goalscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.databinding.FragmentGoalScreenBinding
import com.sillyapps.meantime.ui.ItemClickListener
import com.sillyapps.meantime.ui.ItemTouchHelperCallback
import com.sillyapps.meantime.ui.mainscreen.TaskInfoDialogFragment
import com.sillyapps.meantime.ui.mainscreen.recyclerview.RunningTasksAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoalFragment: Fragment() {

    private val viewModel: GoalViewModel by viewModels(ownerProducer = { this })

    private lateinit var binding: FragmentGoalScreenBinding

    private val args: GoalFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGoalScreenBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.load(args.taskGoalsId)
        setupRecyclerView()

        binding.addGoalFab.setOnClickListener { showGoalDialog() }
    }

    private fun setupRecyclerView() {
        val clickListener = object : ItemClickListener {
            override fun onClickItem(index: Int) {
                showGoalDialog(index)
            }

            override fun onLongClick(index: Int): Boolean {
                return true
            }
        }

        val adapter = GoalsAdapter(clickListener, viewModel)
        binding.goals.adapter = adapter

        val itemTouchHelperCallback = ItemTouchHelperCallback(adapter)
        val touchHelper = ItemTouchHelper(itemTouchHelperCallback)
        touchHelper.attachToRecyclerView(binding.goals)

        viewModel.goals.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })
    }

    private fun showGoalDialog(goalPosition: Int = AppConstants.NOT_ASSIGNED) {
        viewModel.editGoal(goalPosition)
        GoalDialogFragment().show(childFragmentManager, "Goal dialog")
    }
}