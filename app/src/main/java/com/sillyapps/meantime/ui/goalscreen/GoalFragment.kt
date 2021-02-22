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
import com.sillyapps.meantime.setupToolbar
import com.sillyapps.meantime.ui.ItemClickListener
import com.sillyapps.meantime.ui.ItemTouchHelperCallbackNoDrag
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.text.FieldPosition

@AndroidEntryPoint
class GoalFragment: Fragment() {

    private val viewModel: GoalViewModel by viewModels(ownerProducer = { this })

    private lateinit var binding: FragmentGoalScreenBinding

    private val args: GoalFragmentArgs by navArgs()

    private lateinit var smoothScroller: CenterSmoothScroller

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGoalScreenBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        setupToolbar(binding.toolbar)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.load(args.taskGoalsId)
        setupActiveGoals()
        setupCompletedGoals()

        setupTabLayout()

        binding.addGoalFab.setOnClickListener {
            viewModel.createNewGoal()
            showGoalDialog()
        }
    }

    private fun setupActiveGoals() {
        val clickListener = object : ItemClickListener {
            override fun onClickItem(index: Int) {
                viewModel.editActiveGoal(index)
                showGoalDialog()
            }

            override fun onLongClick(index: Int): Boolean {
                viewModel.notifyTaskSelected(index)
                return true
            }
        }

        val callbacks = object : ItemTouchCallbacks {
            override fun onSwiped(position: Int, direction: Int) {
                viewModel.notifyActiveGoalRemoved(position)
            }

            override fun onItemMoved(fromPosition: Int, toPosition: Int) {

            }
        }

        val adapter = GoalsAdapter(clickListener, callbacks)
        binding.goals.adapter = adapter

        val itemTouchHelperCallback = ItemTouchHelperCallbackNoDrag(adapter)
        val touchHelper = ItemTouchHelper(itemTouchHelperCallback)
        touchHelper.attachToRecyclerView(binding.goals)
        smoothScroller = CenterSmoothScroller(binding.goals.context)

        viewModel.activeGoals.observe(viewLifecycleOwner, {
            adapter.submitList(it)
            Timber.d("activegoals loaded")
        })

        viewModel.newGoalAdded.observe(viewLifecycleOwner) {
            adapter.notifyItemInserted(adapter.itemCount)
            binding.goalsTabLayout.apply {
                selectTab(getTabAt(0))
                scrollToPosition(adapter.itemCount-1)
            }
        }

        viewModel.viewModelLoaded.observe(viewLifecycleOwner) {
            val defaultGoalPosition = viewModel.getDefaultGoalPosition()
            if (defaultGoalPosition != AppConstants.NOT_ASSIGNED) {
                scrollToPosition(defaultGoalPosition)
            }
        }
    }

    private fun setupCompletedGoals() {
        val clickListener = object : ItemClickListener {
            override fun onClickItem(index: Int) {
                viewModel.editCompletedGoal(index)
                showGoalDialog()
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

        val itemTouchHelperCallback = ItemTouchHelperCallbackNoDrag(adapter)
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
                    val tabPosition = tab.position
                    viewModel.selectTab(tabPosition)
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (tab.position) {
                        0 -> binding.goals.apply { scrollToPosition(adapter!!.itemCount-1) }
                        else -> binding.completedGoals.apply { scrollToPosition(adapter!!.itemCount-1) }
                    }
                }
            }
        })
    }

    private fun scrollToPosition(position: Int) {
        smoothScroller.targetPosition = position
        binding.goals.layoutManager?.startSmoothScroll(smoothScroller)
    }

    private fun showGoalDialog() {
        GoalDialogFragment().show(childFragmentManager, "Goal dialog")
    }
}