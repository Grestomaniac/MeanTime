package com.sillyapps.meantime.ui.mainscreen

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.sillyapps.meantime.databinding.FragmentMainScreenBinding
import com.sillyapps.meantime.services.DayService
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
        viewDataBinding = FragmentMainScreenBinding.inflate(inflater, container, false).apply {
            this.viewmodel = viewModel
        }

        return viewDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewDataBinding.lifecycleOwner = viewLifecycleOwner

        viewModel.loadDay()

        setupNoTemplateLayout()
    }

    private fun setupNoTemplateLayout() {
        viewModel.noTemplate.observe(viewLifecycleOwner) { noTemplate ->
            if (noTemplate) {
                viewModel.let {
                    it.tasks.removeObservers(viewLifecycleOwner)
                    it.uiTimeRemain.removeObservers(viewLifecycleOwner)
                    it.serviceRunning.removeObservers(viewLifecycleOwner)
                }
            }
            else {
                setupTasksAdapter()
                setupService()
            }
        }
        viewDataBinding.buttonNavigateToEditor.setOnClickListener { navigateToEditor() }
    }

    private fun setupTasksAdapter() {
        val clickListener = object : ItemClickListener {
            override fun onClickItem(index: Int) {
                viewModel.onTaskClicked(index)
                showTaskInfo()
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

        viewModel.tasks.observe(viewLifecycleOwner, {
            it.let { adapter.submitList(it) }
        })

    }

    private fun showTaskInfo() {

    }

    private fun setupService() {
        viewModel.serviceRunning.observe(viewLifecycleOwner) { serviceIsRunning ->
            if (serviceIsRunning) {
                val intent = Intent(activity, DayService::class.java)
                intent.action = DayService.ACTION_START
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    requireActivity().startForegroundService(intent)
                }
                else requireActivity().startService(intent)
            }
        }
    }

    private fun navigateToEditor() {
        findNavController().navigate(MainScreenFragmentDirections.actionMainScreenFragmentToEditTemplateGraph())
    }


}