package com.sillyapps.meantime.ui.mainscreen.recyclerview

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.sillyapps.meantime.ui.ItemTouchHelperAdapter
import com.sillyapps.meantime.ui.ItemTouchHelperCallback
import com.sillyapps.meantime.ui.mainscreen.recyclerview.RunningTasksAdapter

class DayItemTouchHelperCallback(mAdapter: ItemTouchHelperAdapter): ItemTouchHelperCallback(mAdapter) {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        if ((viewHolder as RunningTasksAdapter.ViewHolder).notDraggable) {
            return makeMovementFlags(0, ItemTouchHelper.START)
        }

        return super.getMovementFlags(recyclerView, viewHolder)
    }
}