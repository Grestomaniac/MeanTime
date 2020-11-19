package com.sillyapps.meantime.broadcastrecievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.annotation.CallSuper
import com.sillyapps.meantime.ui.mainscreen.DayManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ScreenOnOffBroadcastReceiver: HiltBroadcastReceiver() {

    @Inject lateinit var dayManager: DayManager

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        
        when (intent?.action) {
            Intent.ACTION_SCREEN_OFF -> {
                Timber.d("Screen is off")
                dayManager.screenIsOff()
            }
            Intent.ACTION_SCREEN_ON -> {
                Timber.d("Screen is on")
                dayManager.screenIsOn()
            }
        }
    }
}

abstract class HiltBroadcastReceiver: BroadcastReceiver() {
    @CallSuper
    override fun onReceive(context: Context?, intent: Intent?) {}
}