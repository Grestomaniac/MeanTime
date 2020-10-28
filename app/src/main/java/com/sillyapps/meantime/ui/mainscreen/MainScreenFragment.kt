package com.sillyapps.meantime.ui.mainscreen

import android.content.Context
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
import timber.log.Timber

@AndroidEntryPoint
class MainScreenFragment: Fragment() {

    private val viewModel by viewModels<MainViewModel>()

    private lateinit var viewDataBinding: FragmentMainScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.preInitialize()
        viewDataBinding = FragmentMainScreenBinding.inflate(inflater, container, false).apply {
            this.viewmodel = viewModel
        }

        Timber.d("onCreateView()")
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewDataBinding.lifecycleOwner = viewLifecycleOwner

        viewModel.currentTask.observe(viewLifecycleOwner) {}

        setupNoTemplateLayout()
        setupTasksAdapter()
        Timber.d("onActivityCreated()")
    }

    private fun setupNoTemplateLayout() {
        viewModel.noTemplate.observe(viewLifecycleOwner) { noTemplate ->
            if (noTemplate) {
                Timber.d("No template")
                viewModel.let {
                    it.currentTask.removeObservers(viewLifecycleOwner)
                    it.tasks.removeObservers(viewLifecycleOwner)
                    it.currentDay.removeObservers(viewLifecycleOwner)
                }
            }
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
//        viewModelStore.clear()
    }

}