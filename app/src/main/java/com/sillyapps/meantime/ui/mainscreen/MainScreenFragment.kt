package com.sillyapps.meantime.ui.mainscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sillyapps.meantime.databinding.FragmentMainScreenBinding
import com.sillyapps.meantime.ui.mainscreen.recyclerview.TasksAdapter
import com.sillyapps.meantime.ui.mainscreen.recyclerview.RecVClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainScreenFragment: Fragment() {

    private val viewModel by viewModels<MainScreenViewModel>()

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

        setupTasksAdapter()
    }

    private fun setupTasksAdapter() {
        val tasksAdapter = TasksAdapter(RecVClickListener { })
        viewDataBinding.tasks.adapter = tasksAdapter

        viewModel.currentDay.observe(viewLifecycleOwner, {
            it.let { tasksAdapter.submitList(it.tasks) }
        })
    }

}