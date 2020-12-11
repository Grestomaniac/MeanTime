package com.sillyapps.meantime.ui.alarmscreen

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.R
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.databinding.ActivityAlarmBinding
import com.sillyapps.meantime.setDarkThemeIfNeeded
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AlarmActivity: AppCompatActivity() {

    private val viewModel: AlarmViewModel by viewModels()

    private lateinit var binding: ActivityAlarmBinding
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDarkThemeIfNeeded()
        viewModel.reload()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_alarm)

        binding.viewmodel = viewModel

        val currentTask = viewModel.completedTask
        if (currentTask.editableSoundOn) {
            setRingtone(currentTask)
        }

        if (currentTask.editableVibrationOn) {
            setVibrator()
        }

        viewModel.timeIsUp.observe(this) { timeIsUp ->
            if (timeIsUp) {
                turnOffAlarm()
            }
        }

        binding.bigAssButton.setOnClickListener { turnOffAlarm() }

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        showActivityIfDeviceIsSleeping()
    }

    private fun setRingtone(currentTask: Task) {
        if (currentTask.editableSoundOn) {
            val soundUri =
                if (currentTask.sound == AppConstants.DEFAULT_RINGTONE)
                    RingtoneManager.getActualDefaultRingtoneUri(applicationContext, RingtoneManager.TYPE_ALARM)
                else Uri.parse(currentTask.sound)

            ringtone = RingtoneManager.getRingtone(applicationContext, soundUri)

            ringtone?.play()
        }
    }

    private fun setVibrator() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(
                    viewModel.alarmDuration,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(viewModel.alarmDuration)
        }
    }

    override fun onPause() {
        super.onPause()

        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (pm.isInteractive) {
            turnOffAlarm()
        }
    }

    override fun onStop() {
        super.onStop()
        vibrator?.cancel()
    }

    private fun turnOffAlarm() {
        ringtone?.stop()
        vibrator?.cancel()

        finish()
    }

    private fun showActivityIfDeviceIsSleeping() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true)
            setShowWhenLocked(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

}