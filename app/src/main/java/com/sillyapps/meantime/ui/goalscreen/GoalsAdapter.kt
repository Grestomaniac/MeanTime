package com.sillyapps.meantime.ui.goalscreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sillyapps.meantime.data.Goal
import com.sillyapps.meantime.databinding.ItemGoalBinding
import com.sillyapps.meantime.ui.ItemClickListener
import com.sillyapps.meantime.ui.ItemTouchHelperAdapter

class GoalsAdapter(private val clickListener: ItemClickListener, private val callbacks: ItemTouchCallbacks): ListAdapter<Goal, GoalsAdapter.ViewHolder>(GoalsDiffCallback()),
    ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, position, clickListener)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        callbacks.onItemMoved(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemDropped(toPosition: Int) {
    }

    override fun onItemSwiped(position: Int, direction: Int) {
        callbacks.onSwiped(position, direction)
        notifyItemRemoved(position)
    }

    class ViewHolder private constructor(private val binding: ItemGoalBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Goal, position: Int, clickListener: ItemClickListener) {
            binding.goal = item
            binding.adapterPosition = position
            binding.clickListener = clickListener
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