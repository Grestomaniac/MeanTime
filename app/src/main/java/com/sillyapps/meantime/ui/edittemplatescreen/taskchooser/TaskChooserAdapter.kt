package com.sillyapps.meantime.ui.edittemplatescreen.taskchooser

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sillyapps.meantime.data.BaseTask
import com.sillyapps.meantime.data.SimpleBaseTask
import com.sillyapps.meantime.databinding.ItemChooserTaskBinding
import com.sillyapps.meantime.ui.ItemClickListener
import com.sillyapps.meantime.utils.formatString

class TaskChooserAdapter(private val clickListener: ViewHolder.OnClick): ListAdapter<SimpleBaseTask, TaskChooserAdapter.ViewHolder>(
    TasksDiffCallback()
) {

    private var unfilteredList = listOf<SimpleBaseTask>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    fun submitUnfilteredList(list: List<SimpleBaseTask>) {
        unfilteredList = list
        submitList(list)
    }

    fun filter(text: String?) {
        if (text.isNullOrBlank()) submitList(unfilteredList)
        else {
            val list = unfilteredList.filter { it.formattedName.contains(formatString(text)) }
            submitList(list)
        }
    }

    class ViewHolder private constructor(private val binding: ItemChooserTaskBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SimpleBaseTask, onClickListener: OnClick) {
            binding.baseTask = item
            binding.root.setOnClickListener { onClickListener.onClick(item) }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding = ItemChooserTaskBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun interface OnClick {
            fun onClick(baseTask: SimpleBaseTask)
        }
    }

}

class TasksDiffCallback: DiffUtil.ItemCallback<SimpleBaseTask>() {
    override fun areItemsTheSame(oldItem: SimpleBaseTask, newItem: SimpleBaseTask): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SimpleBaseTask, newItem: SimpleBaseTask): Boolean {
        return oldItem.formattedName == newItem.formattedName
    }
}