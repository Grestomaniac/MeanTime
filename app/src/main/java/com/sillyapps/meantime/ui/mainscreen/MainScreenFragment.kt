package com.sillyapps.meantime.ui.mainscreen

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.*
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.sillyapps.meantime.R
import com.sillyapps.meantime.databinding.FragmentMainScreenBinding
import com.sillyapps.meantime.services.DayService
import com.sillyapps.meantime.setupToolbar
import com.sillyapps.meantime.utils.tintMenuIcons
import com.sillyapps.meantime.ui.mainscreen.recyclerview.RunningTasksAdapter
import com.sillyapps.meantime.ui.ItemClickListener
import com.sillyapps.meantime.ui.mainscreen.recyclerview.DayItemTouchHelperCallback
import com.sillyapps.meantime.ui.mainscreen.recyclerview.OnStartDragListener
import com.sillyapps.meantime.ui.mainscreen.recyclerview.SwipeToStartCallback
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainScreenFragment: Fragment() {

    private val viewModel: MainViewModel by viewModels(ownerProducer = { this })

    private lateinit var binding: FragmentMainScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainScreenBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        setHasOptionsMenu(true)

        Timber.d("Fragment create view")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("Fragment created")
        binding.lifecycleOwner = viewLifecycleOwner

        setupToolbar(binding.toolbar)
        setupTasksAdapter()
        setupService()

        binding.toolbar.title = viewModel.getTemplateName()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_screen_menu, menu)
        tintMenuIcons(menu.children, requireContext())
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.option_open -> {
                true
            }
            R.id.option_save -> {
                true
            }
            R.id.option_save_as -> {
                true
            }
            R.id.option_load_next -> {
                true
            }
            else -> false
        }

    override fun onResume() {
        super.onResume()
        checkAppVitalPermissions()
    }

    private fun setupRefreshLayout() {
        /*viewDataBinding.refreshDay.setOnRefreshListener {
            viewModel.refreshDay()
        }

        viewModel.refreshing.observe(viewLifecycleOwner) {
            viewDataBinding.refreshDay.isRefreshing = it
        }*/
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

        val onSwipeToEndCallback = object : SwipeToStartCallback {
            override fun swiped(index: Int) {
                navigateToGoalFragment(index)
            }
        }

        val adapter = RunningTasksAdapter(clickListener, viewModel)
        adapter.onSwipeToStartCallback = onSwipeToEndCallback

        binding.runningTasks.adapter = adapter

        val itemTouchHelperCallback = DayItemTouchHelperCallback(adapter)
        val touchHelper = ItemTouchHelper(itemTouchHelperCallback)
        touchHelper.attachToRecyclerView(binding.runningTasks)

        viewModel.uiTasks.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        viewModel.currentTaskStateChanged.observe(viewLifecycleOwner) {
            adapter.notifyItemChanged(viewModel.getCurrentTaskPosition())
        }

        viewModel.taskAdded.observe(viewLifecycleOwner) {
            adapter.notifyItemInserted(adapter.itemCount)
        }
    }

    private fun addTemporalTask() {
        /*viewModel.createTemporalTask()
        TaskDialogFragment().show(childFragmentManager, "Temporal task dialog")*/
    }

    private fun showTaskInfo() {
        /*TaskDialogFragment().show(childFragmentManager, "Task info")*/
    }

    private fun showWarningDialog() {
        WarningDialogFragment().show(childFragmentManager, "Warning info")
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

    private fun checkAppVitalPermissions() {
        val ignoresAppOptimizations = checkIfAppIgnoresBatteryOptimizations()
        val notificationEnabled = NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()

        viewModel.updatePermissionWarnings(ignoresAppOptimizations, notificationEnabled)
    }

    private fun checkIfAppIgnoresBatteryOptimizations(): Boolean {
        val powerManager = requireContext().applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val appPackage = requireContext().applicationContext.packageName

        // TODO make app minSdk as minimal as possible
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return powerManager.isIgnoringBatteryOptimizations(appPackage)
        }
        else return true
    }

    private fun navigateToEditor() {
        findNavController().navigate(MainScreenFragmentDirections.actionMainScreenFragmentToEditTemplateGraph())
    }

    private fun navigateToGoalFragment(taskGoalsId: Int) {
        findNavController().navigate(MainScreenFragmentDirections.actionMainScreenFragmentToGoalFragment(taskGoalsId))
    }

}