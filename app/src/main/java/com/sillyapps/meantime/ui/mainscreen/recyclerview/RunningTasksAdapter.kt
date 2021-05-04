package com.sillyapps.meantime.ui.mainscreen.recyclerview

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MotionEventCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.databinding.ItemMainScreenTaskBinding
import com.sillyapps.meantime.ui.ItemTouchHelperAdapter
import com.sillyapps.meantime.ui.ItemClickListener
import com.sillyapps.meantime.ui.mainscreen.MainViewModel
import timber.log.Timber

class RunningTasksAdapter(private val clickListener: ItemClickListener,
                          private val viewModel: MainViewModel):
    ListAdapter<Task, RunningTasksAdapter.ViewHolder>(TasksDiffCallback()), ItemTouchHelperAdapter {

    var pickedTaskPosition = -1
    var selectedTaskPosition = -1

    var onSwipeToStartCallback: SwipeToStartCallback? = null

    private val adapterClickListener = object : ClickListener {
        override fun onClick(position: Int) {
            if (selectedTaskPosition != -1) {
                getItem(selectedTaskPosition).isSelected = false
                notifyItemChanged(selectedTaskPosition)
            }
            if (selectedTaskPosition != position) {
                getItem(position).isSelected = true
                selectedTaskPosition = position
                notifyItemChanged(position)
                return
            }
            selectedTaskPosition = -1
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getConnectedTask(position)
        holder.bind(item, clickListener, adapterClickListener)
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
        if (position < selectedTaskPosition) selectedTaskPosition--
    }

    class ViewHolder private constructor(private val binding: ItemMainScreenTaskBinding): RecyclerView.ViewHolder(binding.root) {
        var notDraggable: Boolean = false

        fun bind(item: Task, clickListener: ItemClickListener, adapterClickListener: ClickListener) {
            binding.task = item
            notDraggable = item.canNotBeSwappedOrDisabled()
            binding.root.setOnClickListener {
                adapterClickListener.onClick(adapterPosition)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding = ItemMainScreenTaskBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    interface ClickListener {
        fun onClick(position: Int)
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

interface OnStartDragListener {
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
}