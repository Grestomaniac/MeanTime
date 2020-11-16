package com.sillyapps.meantime.broadcastrecievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.sillyapps.meantime.ui.mainscreen.DayManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ScreenOnOffBroadcastReceiver: BroadcastReceiver() {

    @Inject lateinit var dayManager: DayManager

    override fun onReceive(context: Context?, intent: Intent?) {
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