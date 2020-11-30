package com.sillyapps.meantime.ui.goalscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.tabs.TabLayout
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.databinding.FragmentGoalScreenBinding
import com.sillyapps.meantime.ui.ItemClickListener
import com.sillyapps.meantime.ui.ItemTouchHelperCallback
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
    ): View {
        binding = FragmentGoalScreenBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.load(args.taskGoalsId)
        setupActiveGoals()
        setupCompletedGoals()

        setupTabLayout()

        binding.addGoalFab.setOnClickListener { showGoalDialog() }
    }

    private fun setupActiveGoals() {
        val clickListener = object : ItemClickListener {
            override fun onClickItem(index: Int) {
                viewModel.editGoal(index)
                showGoalDialog(index)
            }

            override fun onLongClick(index: Int): Boolean {
                return true
            }
        }

        val callbacks = object : ItemTouchCallbacks {
            override fun onSwiped(position: Int, direction: Int) {
                viewModel.notifyActiveGoalRemoved(position)
            }

            override fun onItemMoved(fromPosition: Int, toPosition: Int) {
                viewModel.notifyActiveGoalSwapped(fromPosition, toPosition)
            }
        }

        val adapter = GoalsAdapter(clickListener, callbacks)
        binding.goals.adapter = adapter

        val itemTouchHelperCallback = ItemTouchHelperCallback(adapter)
        val touchHelper = ItemTouchHelper(itemTouchHelperCallback)
        touchHelper.attachToRecyclerView(binding.goals)

        viewModel.goals.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })
    }

    private fun setupCompletedGoals() {
        val clickListener = object : ItemClickListener {
            override fun onClickItem(index: Int) {

            }

            override fun onLongClick(index: Int): Boolean {
                return true
            }
        }

        val callbacks = object : ItemTouchCallbacks {
            override fun onSwiped(position: Int, direction: Int) {
                if (direction == ItemTouchHelper.END) {
                    viewModel.notifyCompletedGoalRemoved(position)
                }
                else {
                    viewModel.notifyCompletedGoalRecovered(position)
                }
            }

            override fun onItemMoved(fromPosition: Int, toPosition: Int) {
                viewModel.notifyCompletedGoalSwapped(fromPosition, toPosition)
            }
        }

        val adapter = GoalsAdapter(clickListener, callbacks)
        binding.completedGoals.adapter = adapter

        val itemTouchHelperCallback = ItemTouchHelperCallback(adapter)
        val touchHelper = ItemTouchHelper(itemTouchHelperCallback)
        touchHelper.attachToRecyclerView(binding.completedGoals)

        viewModel.completedGoals.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })
    }


    private fun setupTabLayout() {
        binding.goalsTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    val goalTab = GoalViewModel.GoalTab.values()[tab.position]
                    viewModel.selectTab(goalTab)
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    private fun showGoalDialog(goalPosition: Int = AppConstants.NOT_ASSIGNED) {
        GoalDialogFragment().show(childFragmentManager, "Goal dialog")
    }
}