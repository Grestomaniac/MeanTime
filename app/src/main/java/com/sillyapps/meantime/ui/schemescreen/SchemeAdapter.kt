package com.sillyapps.meantime.ui.schemescreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sillyapps.meantime.data.SimplifiedTemplate
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.data.Template
import com.sillyapps.meantime.databinding.ItemEditorTaskBinding
import com.sillyapps.meantime.databinding.ItemSchemeTemplateBinding
import com.sillyapps.meantime.ui.ItemTouchHelperAdapter
import com.sillyapps.meantime.ui.ItemClickListener
import com.sillyapps.meantime.ui.explorer.recyclerview.TemplatesDiffCallback
import timber.log.Timber

class SchemeAdapter(private val viewModel: SchemeViewModel, private val onClickListener: ItemClickListener? = null): ListAdapter<SimplifiedTemplate, SchemeAdapter.ViewHolder>(
    SimplifiedTemplatesDiffCallback()
), ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onClickListener)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        viewModel.notifyItemsSwapped(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemDropped(toPosition: Int) {
    }

    override fun onItemSwiped(position: Int, direction: Int) {
        when (direction) {
            ItemTouchHelper.START -> {
                viewModel.notifyItemDisabled(position)
                notifyItemChanged(position)
            }
            ItemTouchHelper.END -> {
                viewModel.notifyItemRemoved(position)
                notifyItemRemoved(position)
            }
        }
    }

    class ViewHolder private constructor(private val binding: ItemSchemeTemplateBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SimplifiedTemplate, onClickListener: ItemClickListener?) {
            binding.template = item
            binding.root.setOnClickListener { onClickListener?.onClickItem(adapterPosition) }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding = ItemSchemeTemplateBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class SimplifiedTemplatesDiffCallback: DiffUtil.ItemCallback<SimplifiedTemplate>() {
    override fun areItemsTheSame(oldItem: SimplifiedTemplate, newItem: SimplifiedTemplate): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SimplifiedTemplate, newItem: SimplifiedTemplate): Boolean {
        return oldItem.name == newItem.name
    }
}