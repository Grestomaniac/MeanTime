package com.sillyapps.meantime.ui

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.sillyapps.meantime.AppConstants

open class ItemTouchHelperCallback(private val mAdapter: ItemTouchHelperAdapter): ItemTouchHelper.Callback() {

    private var dragTo = AppConstants.NOT_ASSIGNED
    private var dragFrom = AppConstants.NOT_ASSIGNED

    private var dragging = false

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = ItemTouchHelper.START

        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val targetPosition = target.adapterPosition

        mAdapter.onItemMove(viewHolder.adapterPosition, targetPosition)
        dragTo = targetPosition

        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        mAdapter.onItemSwiped(viewHolder.adapterPosition, direction)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            val position = viewHolder!!.adapterPosition
            mAdapter.onItemPicked(position)
            dragFrom = position
            dragging = true
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            if (dragging) when {
                dragTo == AppConstants.NOT_ASSIGNED -> mAdapter.onItemDropped(dragFrom)
                dragFrom > dragTo -> mAdapter.onItemDropped(dragTo)
                else -> mAdapter.onItemDropped(dragFrom)
            }

            dragTo = AppConstants.NOT_ASSIGNED
            dragFrom = AppConstants.NOT_ASSIGNED
            dragging = false
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

    fun onItemPicked(position: Int)

    fun onItemSwiped(position: Int, direction: Int)

}