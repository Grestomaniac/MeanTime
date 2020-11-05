package com.sillyapps.meantime.data

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.sillyapps.meantime.BR

class AppPermissionWarnings: BaseObservable() {
    @Bindable
    var notificationDisabled: Boolean = false
        set(value) {
            field = value
            updateWarning()
            notifyPropertyChanged(BR.notificationDisabled)
        }

    @Bindable
    var batteryOptimizationEnabled: Boolean = false
        set(value) {
            field = value
            updateWarning()
            notifyPropertyChanged(BR.batteryOptimizationEnabled)
        }

    @Bindable
    var haveAnyWarnings: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.haveAnyWarnings)
        }

    private fun updateWarning() {
        haveAnyWarnings = notificationDisabled or batteryOptimizationEnabled
    }
}