package com.sillyapps.meantime.ui.explorer.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sillyapps.meantime.data.Template
import com.sillyapps.meantime.databinding.ItemTemplateBinding
import com.sillyapps.meantime.ui.mainscreen.recyclerview.RecVClickListener

class ExplorerAdapter(private val clickListener: RecVClickListener): ListAdapter<Template, ExplorerAdapter.ViewHolder>(
    TemplateExplorerDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, position, clickListener)
    }

    class ViewHolder private constructor(private val binding: ItemTemplateBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Template, position: Int, clickListener: RecVClickListener) {
            binding.apply {
                template = item
                this.clickListener = clickListener
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding = ItemTemplateBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class TemplateExplorerDiffCallback: DiffUtil.ItemCallback<Template>() {
    override fun areItemsTheSame(oldItem: Template, newItem: Template): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Template, newItem: Template): Boolean {
        return oldItem == newItem
    }
}