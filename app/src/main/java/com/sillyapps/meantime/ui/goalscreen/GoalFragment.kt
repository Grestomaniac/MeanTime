package com.sillyapps.meantime.ui.goalscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import com.sillyapps.meantime.databinding.FragmentGoalScreenBinding
import com.sillyapps.meantime.setupToolbar
import com.sillyapps.meantime.ui.ItemClickListener
import com.sillyapps.meantime.ui.ItemTouchHelperCallbackNoDrag
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

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
        setupGoals()

        binding.addGoalFab.setOnClickListener {
            viewModel.createNewGoal()
            showGoalDialog()
        }
    }

    private fun setupGoals() {
        val clickListener = object : ItemClickListener {
            override fun onClickItem(index: Int) {
                viewModel.editGoal(index)
                showGoalDialog()
            }

            override fun onLongClick(index: Int): Boolean {

                return true
            }
        }

        val callbacks = object : ItemTouchCallbacks {
            override fun onSwiped(position: Int, direction: Int) {
                viewModel.notifyGoalRemoved(position)
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

        viewModel.consolidatedList.observe(viewLifecycleOwner, {
            adapter.submitList(it)
            Timber.d("goals loaded")
        })
    }

    private fun scrollToPosition(position: Int) {
        smoothScroller.targetPosition = position
        binding.goals.layoutManager?.startSmoothScroll(smoothScroller)
    }

    private fun showGoalDialog() {
    }
}