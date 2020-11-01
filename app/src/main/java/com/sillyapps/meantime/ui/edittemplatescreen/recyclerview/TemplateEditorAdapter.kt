package com.sillyapps.meantime.ui.edittemplatescreen.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sillyapps.meantime.AppConstants
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.databinding.ItemEditorTaskBinding
import com.sillyapps.meantime.ui.ItemTouchHelperAdapter
import com.sillyapps.meantime.ui.edittemplatescreen.EditTemplateViewModel
import com.sillyapps.meantime.ui.ItemClickListener

class TemplateEditorAdapter(private val viewModel: EditTemplateViewModel, private val onClickListener: ItemClickListener): ListAdapter<Task, TemplateEditorAdapter.ViewHolder>(TemplateExplorerDiffCallback()), ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, position, onClickListener)
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

    override fun onItemDismiss(position: Int) {
        viewModel.notifyTaskRemoved(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount-position)
    }

    class ViewHolder private constructor(private val binding: ItemEditorTaskBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Task, position: Int, onClickListener: ItemClickListener) {
            binding.task = item
            binding.taskAdapterPosition = position
            binding.onCLickListener = onClickListener
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding = ItemEditorTaskBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class TemplateExplorerDiffCallback: DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.startTime == newItem.startTime
    }
}