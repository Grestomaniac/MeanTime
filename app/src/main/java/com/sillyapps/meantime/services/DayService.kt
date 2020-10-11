package com.sillyapps.meantime.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.sillyapps.meantime.data.repository.AppRepository
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DayService(repository: AppRepository): Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}