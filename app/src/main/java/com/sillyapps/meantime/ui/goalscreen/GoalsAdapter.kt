package com.sillyapps.meantime.ui.goalscreen

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sillyapps.meantime.data.Goal
import com.sillyapps.meantime.databinding.ItemGoalBinding
import com.sillyapps.meantime.ui.ItemClickListener
import com.sillyapps.meantime.ui.ItemTouchHelperAdapter
import com.sillyapps.meantime.ui.ItemTouchHelperAdapterNoDrag
import com.sillyapps.meantime.ui.ItemTouchHelperCallbackNoDrag

class GoalsAdapter(private val clickListener: ItemClickListener, private val callbacks: ItemTouchCallbacks): ListAdapter<Goal, GoalsAdapter.ViewHolder>(GoalsDiffCallback()),
    ItemTouchHelperAdapterNoDrag {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onItemDismiss(position: Int, direction: Int) {
        callbacks.onSwiped(position, direction)
        notifyItemRemoved(position)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    class ViewHolder private constructor(private val binding: ItemGoalBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Goal, clickListener: ItemClickListener) {
            binding.goal = item
            binding.root.setOnClickListener { clickListener.onClickItem(adapterPosition) }
            binding.root.setOnLongClickListener { clickListener.onLongClick(adapterPosition) }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding = ItemGoalBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

}

interface ItemTouchCallbacks {
    fun onSwiped(position: Int, direction: Int)

    fun onItemMoved(fromPosition: Int, toPosition: Int)
}

class GoalsDiffCallback: DiffUtil.ItemCallback<Goal>() {
    override fun areItemsTheSame(oldItem: Goal, newItem: Goal): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Goal, newItem: Goal): Boolean {
        return oldItem == newItem
    }
}

class CenterSmoothScroller(context: Context): LinearSmoothScroller(context) {
    override fun calculateDtToFit(
        viewStart: Int,
        viewEnd: Int,
        boxStart: Int,
        boxEnd: Int,
        snapPreference: Int
    ): Int {
        return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2)
    }
}