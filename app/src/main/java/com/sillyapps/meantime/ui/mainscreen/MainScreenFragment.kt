package com.sillyapps.meantime.ui.mainscreen

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.sillyapps.meantime.databinding.FragmentMainScreenBinding
import com.sillyapps.meantime.services.DayService
import com.sillyapps.meantime.ui.mainscreen.recyclerview.RunningTasksAdapter
import com.sillyapps.meantime.ui.ItemClickListener
import com.sillyapps.meantime.ui.ItemTouchHelperCallback
import com.sillyapps.meantime.ui.mainscreen.recyclerview.SwipeToStartCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_main_screen.*
import timber.log.Timber

@AndroidEntryPoint
class MainScreenFragment: Fragment() {

    private val viewModel: MainViewModel by viewModels(ownerProducer = { this })

    private lateinit var viewDataBinding: FragmentMainScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (viewModel.serviceRunning.value!!) {
            val currentTaskGoalsId = viewModel.getCurrentTaskGoalsId()
            currentTaskGoalsId?.let { navigateToGoalFragment(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding = FragmentMainScreenBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        setHasOptionsMenu(true)
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewDataBinding.lifecycleOwner = viewLifecycleOwner

        setupRefreshLayout()
        setupTasksAdapter()
        setupService()

        addTemporalTaskButton.setOnClickListener { addTemporalTask() }
        viewDataBinding.warningButton.setOnClickListener { showWarningDialog() }
        viewDataBinding.buttonStop.setOnLongClickListener { viewModel.onStopButtonLongClick() }
    }

    override fun onResume() {
        super.onResume()
        checkAppVitalPermissions()
    }

    private fun setupRefreshLayout() {
        viewDataBinding.refreshDay.setOnRefreshListener {
            viewModel.refreshDay()
        }

        viewModel.refreshing.observe(viewLifecycleOwner) {
            viewDataBinding.refreshDay.isRefreshing = it
        }
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

        viewDataBinding.runningTasks.adapter = adapter

        val itemTouchHelperCallback = DayItemTouchHelperCallback(adapter)
        val touchHelper = ItemTouchHelper(itemTouchHelperCallback)
        touchHelper.attachToRecyclerView(viewDataBinding.runningTasks)

        viewModel.tasks.observe(viewLifecycleOwner, {
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
        viewModel.createTemporalTask()
        TemporalTaskDialogFragment().show(childFragmentManager, "Temporal task dialog")
    }

    private fun showTaskInfo() {
        TaskInfoDialogFragment().show(childFragmentManager, "Task info")
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