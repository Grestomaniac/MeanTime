package com.sillyapps.meantime

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SimpleSQLiteQuery
import com.hypertrack.hyperlog.HyperLog
import com.sillyapps.meantime.data.local.AppDatabase
import com.sillyapps.meantime.utils.FileLogger
import com.sillyapps.meantime.utils.formatString

import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class App: Application() {

    @Inject lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()

        makeFakeQueryToDatabase()
        createNotificationChannels()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            HyperLog.initialize(this)
            HyperLog.setLogLevel(Log.DEBUG)
            HyperLog.getDeviceLogsInFile(this)
            FileLogger.initialize(this, true)
        }
    }

    // Ensures what database onCreate callback is executed
    private fun makeFakeQueryToDatabase() {
        database.query("select 1", null)
    }

    private fun createNotificationChannels() {
        createServiceNotificationChannel()
    }

    private fun createServiceNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(AppConstants.SERVICE_MAIN_NOTIFICATION_CHANNEL, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }
}