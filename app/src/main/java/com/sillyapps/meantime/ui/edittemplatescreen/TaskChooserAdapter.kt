package com.sillyapps.meantime.ui.edittemplatescreen

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sillyapps.meantime.data.BaseTask
import com.sillyapps.meantime.databinding.ItemChooserTaskBinding
import com.sillyapps.meantime.ui.ItemClickListener
import com.sillyapps.meantime.utils.formatIfNeeded
import com.sillyapps.meantime.utils.formatString

class TaskChooserAdapter(private val clickListener: ItemClickListener): ListAdapter<BaseTask, TaskChooserAdapter.ViewHolder>(TasksDiffCallback()) {

    private var unfilteredList = listOf<BaseTask>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    fun submitUnfilteredList(list: List<BaseTask>) {
        unfilteredList = list
        submitList(list)
    }

    fun filter(text: String) {
        if (text.isBlank()) submitList(unfilteredList)
        else {
            val list = unfilteredList.filter { it.formattedName.contains(formatString(text)) }
            submitList(list)
        }
    }

    class ViewHolder private constructor(private val binding: ItemChooserTaskBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BaseTask, onClickListener: ItemClickListener) {
            binding.baseTask = item
            binding.root.setOnClickListener { onClickListener.onClickItem(bindingAdapterPosition) }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding = ItemChooserTaskBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

}

class TasksDiffCallback: DiffUtil.ItemCallback<BaseTask>() {
    override fun areItemsTheSame(oldItem: BaseTask, newItem: BaseTask): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: BaseTask, newItem: BaseTask): Boolean {
        return oldItem.formattedName == newItem.formattedName
    }
}