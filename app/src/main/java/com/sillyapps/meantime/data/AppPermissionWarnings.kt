package com.sillyapps.meantime.data

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.sillyapps.meantime.BR
import com.sillyapps.meantime.R
import timber.log.Timber

class AppPermissionWarnings: BaseObservable() {
    @Bindable
    var notificationDisabled: Boolean = false
        set(value) {
            field = value
            updateNotificationWarning()
            notifyPropertyChanged(BR.notificationDisabled)
        }

    @Bindable
    var batteryOptimizationEnabled: Boolean = false
        set(value) {
            field = value
            updateBatteryWarning()
            notifyPropertyChanged(BR.batteryOptimizationEnabled)
        }

    @Bindable
    var haveAnyWarnings: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.haveAnyWarnings)
        }

    @Bindable
    var notificationWarningColor: Int = R.color.colorAlright
        set(value) {
            field = value
            notifyPropertyChanged(BR.notificationWarningColor)
        }
    @Bindable
    var notificationStringResource: Int = R.string.notifications_on
        set(value) {
            field = value
            notifyPropertyChanged(BR.notificationStringResource)
        }

    @Bindable
    var batteryWarningColor: Int = R.color.colorAlright
        set(value) {
            field = value
            notifyPropertyChanged(BR.batteryWarningColor)
        }
    @Bindable
    var batteryStringResource: Int = R.string.battery_optimization_off
        set(value) {
            field = value
            notifyPropertyChanged(BR.batteryStringResource)
        }

    private fun updateNotificationWarning() {
        if (notificationDisabled) {
            notificationStringResource = R.string.notifications_off
            notificationWarningColor = R.color.colorWarning
        }
        else {
            notificationStringResource = R.string.notifications_on
            notificationWarningColor = R.color.colorAlright
        }
        haveAnyWarnings = notificationDisabled or batteryOptimizationEnabled
    }

    private fun updateBatteryWarning() {
        if (batteryOptimizationEnabled) {
            batteryStringResource = R.string.battery_optimization_on
            batteryWarningColor = R.color.colorWarning
        }
        else {
            batteryStringResource = R.string.battery_optimization_off
            batteryWarningColor = R.color.colorAlright
        }
        haveAnyWarnings = notificationDisabled or batteryOptimizationEnabled
    }
}