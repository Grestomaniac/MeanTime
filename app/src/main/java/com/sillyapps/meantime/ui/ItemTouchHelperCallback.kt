package com.sillyapps.meantime.ui

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.sillyapps.meantime.AppConstants
import timber.log.Timber
import kotlin.math.abs

class ItemTouchHelperCallback(private val mAdapter: ItemTouchHelperAdapter): ItemTouchHelper.Callback() {

    var dragTo = AppConstants.NOT_ASSIGNED
    var dragFrom = AppConstants.NOT_ASSIGNED

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
        val targetPosition = target.adapterPosition

        if (dragFrom == AppConstants.NOT_ASSIGNED) {
            dragFrom = viewHolder.adapterPosition
        }

        mAdapter.onItemMove(viewHolder.adapterPosition, targetPosition)
        dragTo = targetPosition
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        mAdapter.onItemDismiss(viewHolder.adapterPosition)
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

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val alpha = ALPHA_FULL - abs(dX) / viewHolder.itemView.width.toFloat()
            viewHolder.itemView.alpha = alpha
            viewHolder.itemView.translationX = dX
        }
        else
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    companion object {
        val ALPHA_FULL = 1.0f
    }

}

interface ItemTouchHelperAdapter {

    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean

    fun onItemDropped(toPosition: Int)

    fun onItemDismiss(position: Int)

}