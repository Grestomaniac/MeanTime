package com.sillyapps.meantime.ui.mainscreen.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.databinding.ItemMainScreenTaskBinding
import com.sillyapps.meantime.ui.ItemTouchHelperAdapter
import com.sillyapps.meantime.ui.ItemClickListener
import com.sillyapps.meantime.ui.mainscreen.MainViewModel

class RunningTasksAdapter(private val clickListener: ItemClickListener, private val viewModel: MainViewModel): ListAdapter<Task, RunningTasksAdapter.ViewHolder>(TasksDiffCallback()),
    ItemTouchHelperAdapter {

    var itemTouchHelperDetachCallback: ItemTouchHelperOnDetachedCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, position, clickListener)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if (fromPosition > toPosition) {
            viewModel.notifyTasksSwapped(toPosition, fromPosition)
        }
        else {
            viewModel.notifyTasksSwapped(fromPosition, toPosition)
        }

        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemDropped(toPosition: Int) {
        viewModel.recalculateStartTimes(toPosition)
        notifyItemRangeChanged(toPosition, itemCount-toPosition)
    }

    override fun onItemSwiped(position: Int) {
        /*Timber.d("On item swiped")
        if (getItem(position).canNotBeSwappedOrDisabled()) {
            Timber.d("Cannot be swiped")
            return
        }
        itemTouchHelperDetachCallback?.onDetach()
        viewModel.notifyTaskDisabled(position)
        notifyItemRangeChanged(position, itemCount-position)*/
    }

    class ViewHolder private constructor(private val binding: ItemMainScreenTaskBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Task, position: Int, clickListener: ItemClickListener) {
            binding.task = item
            binding.taskAdapterPosition = position
            binding.clickListener = clickListener
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding = ItemMainScreenTaskBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class TasksDiffCallback: DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        // true если два объекта имеют одинаковые ссылки, нужно было что-то вроде PrimaryKey использовать,
        // но думаю для этого адаптера и этого хватит
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        // true если эти два объекта имеют одинаковое содержимое
        return oldItem.startTime == newItem.startTime
    }
}

interface ItemTouchHelperOnDetachedCallback {
    fun onDetach()
}