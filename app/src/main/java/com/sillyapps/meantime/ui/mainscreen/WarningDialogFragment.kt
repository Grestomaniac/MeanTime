package com.sillyapps.meantime.ui.mainscreen

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.R
import com.sillyapps.meantime.databinding.DialogWarningsBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class WarningDialogFragment: DialogFragment() {

    private val viewModel: MainViewModel by viewModels( ownerProducer = { requireParentFragment() } )

    private lateinit var binding: DialogWarningsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogWarningsBinding.inflate(inflater, container, false)
        binding.appWarnings = viewModel.appPermissionWarnings.value

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.batteryOptimizationLayout.setOnClickListener { onBatteryOptimizationWarningClick() }
        binding.notificationLayout.setOnClickListener { onNotificationWarningClick() }
    }

    private fun onBatteryOptimizationWarningClick() {
        if (!viewModel.appPermissionWarnings.value!!.batteryOptimizationEnabled) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            startActivity(intent)
        }
        else {
            return
        }
    }

    private fun onNotificationWarningClick() {
        if (!viewModel.appPermissionWarnings.value!!.notificationDisabled) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, AppConstants.SERVICE_MAIN_NOTIFICATION_CHANNEL)
            }
            startActivity(intent)
        }
        else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", requireContext().packageName, null)
            startActivity(intent)
            // Toast.makeText(context, getString(R.string.notification_settings_path, name), Toast.LENGTH_LONG).show()
        }

    }
}