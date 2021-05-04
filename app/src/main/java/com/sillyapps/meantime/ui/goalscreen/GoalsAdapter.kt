package com.sillyapps.meantime.ui.goalscreen

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sillyapps.meantime.data.Goal
import com.sillyapps.meantime.databinding.ItemGoalBinding
import com.sillyapps.meantime.databinding.ItemTagBinding
import com.sillyapps.meantime.ui.ItemClickListener
import com.sillyapps.meantime.ui.ItemTouchHelperAdapterNoDrag

class GoalsAdapter(private val clickListener: ItemClickListener, private val callbacks: ItemTouchCallbacks): ListAdapter<ListItem, GoalsAdapter.BindableViewHolder>(GoalsDiffCallback()),
    ItemTouchHelperAdapterNoDrag {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindableViewHolder {
        return when (viewType) {
            ListItem.TYPE_TAG -> TagViewHolder.from(parent)
            else -> GoalViewHolder.from(parent)
        }
    }

    override fun onItemDismiss(position: Int, direction: Int) {
        callbacks.onSwiped(position, direction)
        notifyItemRemoved(position)
    }

    override fun onBindViewHolder(holder: BindableViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    abstract class BindableViewHolder(root: View): RecyclerView.ViewHolder(root)  {
        open fun bind(item: ListItem, clickListener: ItemClickListener) {}
    }

    class GoalViewHolder private constructor(private val binding: ItemGoalBinding): BindableViewHolder(binding.root) {

        override fun bind(item: ListItem, clickListener: ItemClickListener) {
            binding.goal = item.getData() as Goal
            binding.root.setOnClickListener { clickListener.onClickItem(adapterPosition) }
            binding.root.setOnLongClickListener { clickListener.onLongClick(adapterPosition) }
        }

        companion object {
            fun from(parent: ViewGroup): GoalViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding = ItemGoalBinding.inflate(layoutInflater, parent, false)
                return GoalViewHolder(binding)
            }
        }
    }

    class TagViewHolder private constructor(private val binding: ItemTagBinding): BindableViewHolder(binding.root) {

        override fun bind(item: ListItem, clickListener: ItemClickListener) {
            binding.tagName = item.getData() as String
        }

        companion object {
            fun from(parent: ViewGroup): TagViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding = ItemTagBinding.inflate(layoutInflater, parent, false)
                return TagViewHolder(binding)
            }
        }
    }

    class GoalsDiffCallback: DiffUtil.ItemCallback<ListItem>() {
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem.getData() == newItem.getData()
        }

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem == newItem
        }
    }

}

interface ItemTouchCallbacks {
    fun onSwiped(position: Int, direction: Int)

    fun onItemMoved(fromPosition: Int, toPosition: Int)
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

abstract class ListItem {
    companion object {
        const val TYPE_TAG = 0
        const val TYPE_GENERAL = 0

        const val NO_DATA = "NO DATA"
    }

    open fun getType(): Int {
        return TYPE_GENERAL
    }

    open fun getData(): Any {
        return NO_DATA
    }

    override fun equals(other: Any?): Boolean {
        other as ListItem
        return (getType() == other.getType()) and (getData() == other.getData())
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

class TagItem(private val tag: String): ListItem() {
    override fun getType(): Int {
        return TYPE_TAG
    }

    override fun getData(): String {
        return tag
    }
}

class GoalItem(val goal: Goal): ListItem() {

    override fun getData(): Any {
        return goal
    }
}