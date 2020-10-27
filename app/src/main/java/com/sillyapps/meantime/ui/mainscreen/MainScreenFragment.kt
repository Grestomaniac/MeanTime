package com.sillyapps.meantime.ui.mainscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.sillyapps.meantime.NOT_ASSIGNED
import com.sillyapps.meantime.databinding.FragmentMainScreenBinding
import com.sillyapps.meantime.ui.mainscreen.recyclerview.RunningTasksAdapter
import com.sillyapps.meantime.ui.ItemClickListener
import com.sillyapps.meantime.ui.ItemTouchHelperCallback
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainScreenFragment: Fragment() {

    private val viewModel by viewModels<MainViewModel>()

    private lateinit var viewDataBinding: FragmentMainScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = FragmentMainScreenBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }

        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner

        viewModel.currentTask.observe(this.viewLifecycleOwner) {}

        setupNoTemplateLayout()
        setupTasksAdapter()

    }

    private fun setupNoTemplateLayout() {
        viewDataBinding.buttonNavigateToEditor.setOnClickListener { navigateToEditor() }
    }

    private fun setupTasksAdapter() {
        val clickListener = object : ItemClickListener {
            override fun onClickItem(index: Int) {
                // TODO show task info
            }

            override fun onLongClick(index: Int): Boolean {
                return true
            }

        }

        val adapter = RunningTasksAdapter(clickListener, viewModel)
        viewDataBinding.tasks.adapter = adapter

        val itemTouchHelperCallback = ItemTouchHelperCallback(adapter)
        val touchHelper = ItemTouchHelper(itemTouchHelperCallback)
        touchHelper.attachToRecyclerView(viewDataBinding.tasks)

        viewModel.tasks.observe(viewLifecycleOwner) {}

        viewModel.tasks.observe(viewLifecycleOwner, {
            it.let { adapter.submitList(it) }

        })

    }

    private fun navigateToEditor() {
        findNavController().navigate(MainScreenFragmentDirections.actionMainScreenFragmentToEditTemplateGraph())
    }

}