package com.sillyapps.meantime.ui

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.sillyapps.meantime.R
import com.sillyapps.meantime.databinding.CustomSearchViewBinding

class CustomSearchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttrs: Int = 0): ConstraintLayout(context, attrs, defStyleAttrs, R.style.DefaultCustomSearchViewStyle) {

    private val binding: CustomSearchViewBinding = CustomSearchViewBinding.inflate(LayoutInflater.from(context), this, true)

    var textChangedListener: TextChangedListener? = null

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable?) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            textChangedListener?.onTextChanged(s.toString())
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding.searchField.addTextChangedListener(textWatcher)
        binding.clearButton.setOnClickListener { onClearButtonClick() }
    }

    private fun onClearButtonClick() {
        binding.searchField.setText("")
    }

    fun interface TextChangedListener {
        fun onTextChanged(text: String)
    }

}