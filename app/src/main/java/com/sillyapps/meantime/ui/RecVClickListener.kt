package com.sillyapps.meantime.ui

interface RecVClickListener {
    fun onClickItem(index: Int)
    fun onLongClick(index: Int): Boolean
}