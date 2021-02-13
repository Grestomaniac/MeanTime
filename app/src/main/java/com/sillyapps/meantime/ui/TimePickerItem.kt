package com.sillyapps.meantime.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.UserManager
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.UserManagerCompat
import androidx.core.text.isDigitsOnly
import com.sillyapps.meantime.R
import com.sillyapps.meantime.databinding.ItemTimePickerBinding
import com.sillyapps.meantime.hideKeyBoard
import com.sillyapps.meantime.utils.formatTime
import com.sillyapps.meantime.utils.getActivity
import kotlinx.coroutines.*
import timber.log.Timber

class TimePickerItem @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttrs: Int = 0): ConstraintLayout(context, attrs, defStyleAttrs), View.OnFocusChangeListener {
    val binding = ItemTimePickerBinding.inflate(LayoutInflater.from(context), this, true)

    private val longClickIncAmount: Int
    private val maxValue: Int
    var previousView: TimePickerItem? = null
    var nextView: TimePickerItem? = null
        set(value) {
            field = value
            binding.picker.nextFocusForwardId = value!!.id
        }

    private lateinit var watcher: TextWatcher

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.TimePickerItem, 0, 0).apply {
            maxValue = getInt(R.styleable.TimePickerItem_maxValue, 59)
            longClickIncAmount = getInt(R.styleable.TimePickerItem_longClickIncAmount, 5)
        }
    }

    fun setAsLastPicker() {
        binding.picker.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.picker.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                clearPickerFocus(v)
            }
            return@setOnEditorActionListener false
        }
    }

    private fun clearPickerFocus(v: View) {
        v.clearFocus()
        getActivity()?.hideKeyBoard(v)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setTimeTextListener()
    }

    fun setTime(value: Int) {
        setTextSafely(formatTime(value))
    }

    fun getTime(): Int {
        return binding.picker.text.toString().toInt()
    }

    private fun setTimeTextListener() {
        watcher = object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrBlank()) return
                if (s.length > 2) {
                    setTextSafely(s.subSequence(2, 3))
                    binding.picker.setSelection(binding.picker.text.length)
                    return
                }

                val value = s.toString().toInt()
                if (value > maxValue) {
                    focusToNextView(formatTime(maxValue))
                }

                else if (value*10 > maxValue) {
                    focusToNextView(formatTime(value))
                }
            }

            private fun focusToNextView(value: CharSequence?) {
                setTextSafely(value)

                if (nextView != null) {
                    nextView?.requestFocus()
                }
                else {
                    clearFocus()
                    getActivity()?.hideKeyBoard(this@TimePickerItem)
                }
            }
        }

        binding.arrowUp.also {
            it.setOnClickListener { incrementTime() }
            it.setOnLongClickListener { incrementTime(longClickIncAmount) }

            it.setOnTouchListener(RepeatListener())
        }

        binding.arrowDown.also {
            it.setOnClickListener { decrementTime() }
            it.setOnLongClickListener { decrementTime(longClickIncAmount) }

            it.setOnTouchListener(RepeatListener())
        }

        binding.picker.filters = arrayOf(TimePickerInputFilter())
        binding.picker.addTextChangedListener(watcher)

        onFocusChangeListener = this
    }

    private fun incrementTime(amount: Int = 1): Boolean {
        val value = binding.picker.text.toString().toInt() + amount

        if (value <= maxValue)
            setTextSafely(formatTime(value))
        else {
            previousView?.incrementTime()
            setTextSafely(formatTime(value % maxValue - 1))
        }
        return true
    }

    private fun decrementTime(amount: Int = 1): Boolean {
        val value = binding.picker.text.toString().toInt() - amount

        if (value >= 0)
            setTextSafely(formatTime(value))
        else {
            previousView?.decrementTime()
            setTextSafely(formatTime(maxValue - amount + 1))
        }
        return true
    }

    fun setTextSafely(value: CharSequence?) {
        binding.picker.removeTextChangedListener(watcher)
        binding.picker.setText(value)
        binding.picker.addTextChangedListener(watcher)
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (!hasFocus) {
            val value =
                if (binding.picker.text.isNotBlank()) binding.picker.text.toString().toInt()
                else 0
            setTextSafely(formatTime(value))
        }
    }

}

class RepeatListener : View.OnTouchListener {

    private val handler = Handler(Looper.getMainLooper())

    private var touchedView: View? = null
    private var haveClicked = false

    private var clickInterval = ViewConfiguration.getLongPressTimeout().toLong()
    private var repeatInterval = 1000L

    private val handlerRunnable: Runnable = run {
        Runnable {
            haveClicked = true
            handler.postDelayed(handlerRunnable, repeatInterval)
            touchedView?.performLongClick()
        }
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                handler.removeCallbacks(handlerRunnable)

                handler.postDelayed(handlerRunnable, clickInterval)
                touchedView = view
                haveClicked = false
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(handlerRunnable)
                if (!haveClicked)
                    touchedView?.performClick()
                touchedView = null
                return true
            }
        }

        return false
    }
}
class TimePickerInputFilter(): InputFilter {
    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
        source?.let {
            if (it.isEmpty() or it.isDigitsOnly())
                return null
        }
        return ""
    }
}