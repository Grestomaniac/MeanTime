package com.sillyapps.meantime

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sillyapps.meantime.data.local.AppDatabase

import dagger.hilt.android.HiltAndroidApp
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