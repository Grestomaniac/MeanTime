package com.sillyapps.meantime.ui.mainscreen.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.data.State
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.databinding.ItemMainScreenTaskBinding
import com.sillyapps.meantime.ui.ItemTouchHelperAdapter
import com.sillyapps.meantime.ui.ItemClickListener
import com.sillyapps.meantime.ui.mainscreen.MainViewModel
import timber.log.Timber

class RunningTasksAdapter(private val clickListener: ItemClickListener, private val viewModel: MainViewModel): ListAdapter<Task, RunningTasksAdapter.ViewHolder>(TasksDiffCallback()),
    ItemTouchHelperAdapter {

    var pickedTaskPosition = -1

    var onSwipeToStartCallback: SwipeToStartCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getConnectedTask(position)
        holder.bind(item, clickListener)
    }

    private fun getConnectedTask(position: Int): Task {
        val item = getItem(position)
        if (position != 0) {
            val prevTask = getItem(position - 1)
            prevTask.hasNextTask = true
            item.hasPrevTask = true
        }
        return item
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val task = getItem(toPosition)
        if (fromPosition > toPosition) {
            if (task.canNotBeSwappedOrDisabled()) {
                return true
            }
            viewModel.notifyTasksSwapped(toPosition, fromPosition)

            if (toPosition > 0) getItem(toPosition-1).disconnectNext()
            connectNext(task, fromPosition)
        }
        else {
            viewModel.notifyTasksSwapped(fromPosition, toPosition)

            if (toPosition < itemCount-1) getItem(toPosition+1).disconnectPrev()
            connectPrev(task, fromPosition)
        }
        pickedTaskPosition = toPosition

        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemDropped(toPosition: Int) {
        viewModel.recalculateStartTimes(toPosition)

        connectTask()
    }

    private fun connectNext(task: Task, position: Int) {
        task.disconnectPrev()
        if (position < itemCount-1) {
            task.connectNext()
            getItem(position+1).connectPrev()
        }
    }

    private fun connectPrev(task: Task, position: Int) {
        task.disconnectNext()
        if (position > 0) {
            task.connectPrev()
            getItem(position-1).connectNext()
        }
    }

    private fun connectTask() {
        val task = getItem(pickedTaskPosition)
        if (pickedTaskPosition > 0) {
            task.connectPrev()
            getItem(pickedTaskPosition-1).connectNext()
        }
        if (pickedTaskPosition < itemCount-1) {
            task.connectNext()
            getItem(pickedTaskPosition+1).connectPrev()
        }
    }

    override fun onItemPicked(position: Int) {
        val task = getItem(position)
        if (task.hasPrevTask) {
            task.disconnectPrev()
            getItem(position-1).disconnectNext()
        }
        if (task.hasNextTask) {
            task.disconnectNext()
            getItem(position+1).disconnectPrev()
        }
        pickedTaskPosition = position
    }

    override fun onItemSwiped(position: Int, direction: Int) {
        viewModel.notifyTaskDisabled(position)
        notifyItemChanged(position)
    }

    class ViewHolder private constructor(private val binding: ItemMainScreenTaskBinding): RecyclerView.ViewHolder(binding.root) {
        var notDraggable: Boolean = false

        fun bind(item: Task, clickListener: ItemClickListener) {
            binding.task = item
            binding.root.setOnClickListener { clickListener.onClickItem(adapterPosition) }
            notDraggable = item.canNotBeSwappedOrDisabled()
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

interface SwipeToStartCallback {
    fun swiped(index: Int)
}