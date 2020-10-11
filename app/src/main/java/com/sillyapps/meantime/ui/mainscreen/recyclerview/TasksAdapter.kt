package com.sillyapps.meantime.ui.mainscreen.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.databinding.ItemTaskBinding

class TasksAdapter(private val clickListener: RecVClickListener): ListAdapter<Task, TasksAdapter.ViewHolder>(
    TasksDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, position, clickListener)
    }

    class ViewHolder private constructor(private val binding: ItemTaskBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Task, position: Int, clickListener: RecVClickListener) {
            binding.task = item
            binding.taskAdapterPosition = position
            binding.clickListener = clickListener
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding = ItemTaskBinding.inflate(layoutInflater, parent, false)
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
        return oldItem == newItem
    }
}