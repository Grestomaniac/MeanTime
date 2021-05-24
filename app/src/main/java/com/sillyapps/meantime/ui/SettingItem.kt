package com.sillyapps.meantime.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.R
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.databinding.ItemEditorSettingBinding
import com.sillyapps.meantime.utils.convertMillisToStringFormat
import com.sillyapps.meantime.utils.getHoursAndMinutes
import com.sillyapps.meantime.utils.getVerboseTime

class SettingItem @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttrs: Int = 0): ConstraintLayout(context, attrs, defStyleAttrs) {
    val binding = ItemEditorSettingBinding.inflate(LayoutInflater.from(context), this, true)

    private val label: String?
    private val iconResId: Int
    private val leftBarResId: Int
    private val settingContent: String?

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.SettingItem, 0, 0).apply {
            label = getString(R.styleable.SettingItem_settingName)
            settingContent = getString(R.styleable.SettingItem_settingContent)
            iconResId = getResourceId(R.styleable.SettingItem_icon, -1)
            leftBarResId = getResourceId(R.styleable.SettingItem_leftBarColor, -1)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        binding.apply {
            settingName.text = label
            content.text = settingContent
            if (iconResId != -1) icon.setImageResource(iconResId)
            if (leftBarResId != -1) leftBar.setBackgroundResource(leftBarResId)
        }
        setBackgroundResource(R.color.itemColor)
        elevation = resources.getDimension(R.dimen.itemDefaultElevation)
    }
}

@BindingAdapter("app:contentString")
fun SettingItem.setContentString(value: String?) {
    binding.content.text = value
}

@BindingAdapter("app:contentTime")
fun SettingItem.setContentLong(duration: Long) {
    binding.content.text = convertMillisToStringFormat(duration)
}

@BindingAdapter("app:contentBreak")
fun SettingItem.setContentBreak(taskBreak: Task.Break) {
    if (!taskBreak.hasBreak) {
        binding.content.text = context.getText(R.string.no_break)
        return
    }
    val intervalText = getVerboseTime(taskBreak.breakInterval, context)
    val durationText = getVerboseTime(taskBreak.breakDuration, context)

    binding.content.text = context.getString(R.string.break_format, intervalText, durationText)
}

@BindingAdapter("soundName")
fun SettingItem.setSoundString(uriPath: String) {
    if (uriPath == AppConstants.DEFAULT_RINGTONE) {
        binding.content.text = context.getString(R.string.default_ringtone)
        return
    }

    val cursor = context.contentResolver.query(Uri.parse(uriPath), null, null, null, null)
    cursor?.moveToFirst()
    binding.content.text = cursor?.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
    cursor?.close()
}