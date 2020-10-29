package com.sillyapps.meantime.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelProvider
import com.sillyapps.meantime.data.repository.AppRepository
import com.sillyapps.meantime.ui.mainscreen.DayManager
import com.sillyapps.meantime.ui.mainscreen.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DayService(): Service() {

    @Inject lateinit var dayManager: DayManager

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}