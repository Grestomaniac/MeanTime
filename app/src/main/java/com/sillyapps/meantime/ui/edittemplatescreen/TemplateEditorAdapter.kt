package com.sillyapps.meantime.ui.edittemplatescreen.recyclerview

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.maltaisn.icondialog.pack.IconPack
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.databinding.ItemEditorTaskBinding
import com.sillyapps.meantime.ui.ItemTouchHelperAdapter
import com.sillyapps.meantime.ui.edittemplatescreen.EditTemplateViewModel
import com.sillyapps.meantime.ui.ItemClickListener
import timber.log.Timber
import javax.inject.Inject

class TemplateEditorAdapter(private val viewModel: EditTemplateViewModel, private val onClickListener: ItemClickListener): ListAdapter<Task, TemplateEditorAdapter.ViewHolder>(
    TasksDiffCallback()
), ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        Timber.d("baseTasks is null = ${viewModel.tasksBaseTasks.value == null}")
        val iconId = viewModel.tasksBaseTasks.value?.get(position)?.iconResId
        Timber.d("Icon id == $iconId")
        val iconDrawable = viewModel.getDrawableForIcon(iconId)
        holder.bind(item, onClickListener, iconDrawable)
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

    override fun onItemPicked(position: Int) {

    }

    override fun onItemSwiped(position: Int, direction: Int) {
        viewModel.notifyTaskRemoved(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount-position)
    }

    class ViewHolder private constructor(private val binding: ItemEditorTaskBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Task, onClickListener: ItemClickListener, iconDrawable: Drawable?) {
            binding.task = item
            binding.taskIcon.setImageDrawable(iconDrawable)
            binding.root.setOnClickListener { onClickListener.onClickItem(bindingAdapterPosition) }
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

class TasksDiffCallback: DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.startTime == newItem.startTime
    }
}