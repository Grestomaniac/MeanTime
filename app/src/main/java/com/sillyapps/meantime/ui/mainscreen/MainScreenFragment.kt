package com.sillyapps.meantime.ui.mainscreen

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.sillyapps.meantime.data.AppPermissionWarnings
import com.sillyapps.meantime.databinding.FragmentMainScreenBinding
import com.sillyapps.meantime.services.DayService
import com.sillyapps.meantime.ui.mainscreen.recyclerview.RunningTasksAdapter
import com.sillyapps.meantime.ui.ItemClickListener
import com.sillyapps.meantime.ui.ItemTouchHelperCallback
import com.sillyapps.meantime.ui.TimePickerFragment
import com.sillyapps.meantime.ui.edittemplatescreen.EditTaskFragment
import com.sillyapps.meantime.ui.mainscreen.recyclerview.ItemTouchHelperOnDetachedCallback
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainScreenFragment: Fragment() {

    private val viewModel: MainViewModel by viewModels(ownerProducer = { this })

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
        viewDataBinding.warningButton.setOnClickListener { showWarningDialog() }
    }

    override fun onResume() {
        super.onResume()
        checkAppVitalPermissions()
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

        adapter.itemTouchHelperDetachCallback = object : ItemTouchHelperOnDetachedCallback {
            override fun onDetach() {
                touchHelper.attachToRecyclerView(null)
                touchHelper.attachToRecyclerView(viewDataBinding.tasks)
            }
        }

        viewModel.tasks.observe(viewLifecycleOwner, {
            it.let { adapter.submitList(it) }
        })

    }

    private fun showTaskInfo() {

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

        Timber.d("ignoresAppOptimizations = $ignoresAppOptimizations; notifications enabled = $notificationEnabled")
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


}