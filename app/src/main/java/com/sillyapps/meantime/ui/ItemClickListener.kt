package com.sillyapps.meantime.ui

interface ItemClickListener {
    fun onClickItem(index: Int)
    fun onLongClick(index: Int): Boolean
}