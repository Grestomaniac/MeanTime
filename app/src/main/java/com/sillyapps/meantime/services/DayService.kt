package com.sillyapps.meantime.services

import android.app.*
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.databinding.Observable
import com.sillyapps.meantime.*
import com.sillyapps.meantime.broadcastrecievers.ScreenOnOffBroadcastReceiver
import com.sillyapps.meantime.data.State
import com.sillyapps.meantime.ui.alarmscreen.AlarmActivity
import com.sillyapps.meantime.ui.mainscreen.DayManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.lang.IllegalArgumentException
import javax.inject.Inject

@AndroidEntryPoint
class DayService: Service() {
    companion object {
        const val SERVICE_ID = 1
        const val ACTION_STOP = "STOP"
        const val ACTION_START = "START"
        const val ACTION_RESUME = "RESUME"
        const val ACTION_PAUSE = "PAUSE"
    }

    @Inject lateinit var dayManager: DayManager
    private lateinit var intentToActivity: PendingIntent

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var pausedNotificationBuilder: NotificationCompat.Builder

    private lateinit var pauseIntent: PendingIntent
    private lateinit var stopIntent: PendingIntent
    private lateinit var resumeIntent: PendingIntent

    private lateinit var notificationManager: NotificationManager

    private lateinit var screenOnOffReceiver: ScreenOnOffBroadcastReceiver

    private val notificationUpdateCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            when (propertyId) {
                BR.timeRemain -> updateTimer()

                AppBR.taskFinishedNaturally -> turnOnAlarm()

                BR.dayState -> handleStateChange()

                BR.isRunning -> {
                    stopService()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        registerScreenOnOffReceiver()
        setupObservers()

        setupButtonIntents()
        setupNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                ACTION_STOP -> {
                    dayManager.stopTask()
                    updateTimer()
                }

                ACTION_PAUSE -> {
                    dayManager.pauseDay()
                    pauseService()
                }

                ACTION_RESUME -> {
                    dayManager.start()
                    resumeService()
                }
            }
        }

        return START_STICKY
    }

    private fun registerScreenOnOffReceiver() {
        screenOnOffReceiver = ScreenOnOffBroadcastReceiver()
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenOnOffReceiver, intentFilter)
    }

    private fun unregisterScreenOnOffReceiver() {
        try {
            unregisterReceiver(screenOnOffReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private fun setupObservers() {
        // Observe changes on day, if change update notification
        dayManager.thisDay!!.addOnPropertyChangedCallback(notificationUpdateCallback)
    }

    private fun setupNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        intentToActivity = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        setNotificationBuilder()
        startForeground(SERVICE_ID, notificationBuilder.build())
    }

    private fun setupButtonIntents() {
        stopIntent = Intent(this, DayService::class.java).let {
            it.action = ACTION_STOP
            PendingIntent.getService(this, 0, it, 0)
        }
        pauseIntent = Intent(this, DayService::class.java).let {
            it.action = ACTION_PAUSE
            PendingIntent.getService(this, 0, it, 0)
        }
        resumeIntent = Intent(this, DayService::class.java).let {
            it.action = ACTION_RESUME
            PendingIntent.getService(this, 0, it, 0)
        }
    }

    private fun setNotificationBuilder() {
        val currentDay = dayManager.thisDay!!
        val timeRemain = convertMillisToStringFormatWithSeconds(currentDay.timeRemain)

        notificationBuilder =  NotificationCompat.Builder(this, AppConstants.SERVICE_MAIN_NOTIFICATION_CHANNEL)
            .setContentTitle(currentDay.currentTask.name)
            .setContentText(timeRemain)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(intentToActivity)
            .setAutoCancel(false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.ic_pause, getString(R.string.pause), pauseIntent)
            .addAction(R.drawable.ic_stop_black_24dp, getString(R.string.stop), stopIntent)

        pausedNotificationBuilder = NotificationCompat.Builder(this, AppConstants.SERVICE_MAIN_NOTIFICATION_CHANNEL)
            .setContentTitle(currentDay.currentTask.name)
            .setContentText(timeRemain)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(intentToActivity)
            .setAutoCancel(false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.ic_play, getString(R.string.start), resumeIntent)
    }

    private fun updateTimer() {
        val currentDay = dayManager.thisDay!!
        val timeRemain = convertMillisToStringFormatWithSeconds(currentDay.timeRemain)
        notificationBuilder.setContentText(timeRemain)
        notificationBuilder.setContentTitle(currentDay.currentTask.name)

        notificationManager.notify(SERVICE_ID, notificationBuilder.build())
    }

    private fun handleStateChange() {
        when (dayManager.thisDay!!.dayState) {
            State.DISABLED -> pauseService()
            State.ACTIVE -> resumeService()
            else -> return
        }
    }

    private fun stopService() {
        stopSelf()
    }

    private fun pauseService() {
        val currentDay = dayManager.thisDay!!
        val timeRemain = convertMillisToStringFormatWithSeconds(currentDay.timeRemain)
        pausedNotificationBuilder.setContentText(timeRemain)
        pausedNotificationBuilder.setContentTitle(currentDay.currentTask.name)
        notificationManager.notify(SERVICE_ID, pausedNotificationBuilder.build())
    }

    private fun resumeService() {
        updateTimer()
    }

    private fun turnOnAlarm() {
        val alarmIntent = Intent(this, AlarmActivity::class.java)
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK + Intent.FLAG_ACTIVITY_CLEAR_TASK)

        startActivity(alarmIntent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterScreenOnOffReceiver()
    }
}