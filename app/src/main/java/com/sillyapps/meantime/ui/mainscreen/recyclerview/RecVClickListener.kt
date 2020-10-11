package com.sillyapps.meantime.ui.mainscreen.recyclerview

import javax.inject.Inject

class RecVClickListener(val clickListener: (taskIndex: Int) -> Unit) {
    fun onClickItem(taskIndex: Int) = clickListener(taskIndex)
}