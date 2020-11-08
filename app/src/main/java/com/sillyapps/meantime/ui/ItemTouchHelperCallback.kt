package com.sillyapps.meantime.ui

import android.graphics.Canvas
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.sillyapps.meantime.AppConstants
import timber.log.Timber
import kotlin.math.abs

class ItemTouchHelperCallback(private val mAdapter: ItemTouchHelperAdapter): ItemTouchHelper.Callback() {

    var dragTo = AppConstants.NOT_ASSIGNED
    var dragFrom = AppConstants.NOT_ASSIGNED

    var waitingForAnimationToEnd = false

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = ItemTouchHelper.END or ItemTouchHelper.START

        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {

        return true
    }

    override fun onMoved(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        fromPos: Int,
        target: RecyclerView.ViewHolder,
        toPos: Int,
        x: Int,
        y: Int
    ) {
        Timber.d("onMoved")
        val targetPosition = target.adapterPosition

        if (dragFrom == AppConstants.NOT_ASSIGNED) {
            dragFrom = viewHolder.adapterPosition
        }

        mAdapter.onItemMove(viewHolder.adapterPosition, targetPosition)
        dragTo = targetPosition
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        Timber.d("clearView")
        super.clearView(recyclerView, viewHolder)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        if ((actionState == ItemTouchHelper.ACTION_STATE_IDLE) and (dragTo != AppConstants.NOT_ASSIGNED)) {
            if (dragFrom > dragTo)
                mAdapter.onItemDropped(dragTo)
            else
                mAdapter.onItemDropped(dragFrom)

            dragTo = AppConstants.NOT_ASSIGNED
            dragFrom = AppConstants.NOT_ASSIGNED
        }
    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    companion object {
        val ALPHA_FULL = 1f
    }

}

interface ItemTouchHelperAdapter {

    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean

    fun onItemDropped(toPosition: Int)

    fun onItemSwiped(position: Int)

}