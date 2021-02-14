package com.sillyapps.meantime.ui.schemescreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sillyapps.meantime.data.SchemeTemplate
import com.sillyapps.meantime.data.SchemeTemplateInfo
import com.sillyapps.meantime.databinding.ItemSchemeTemplateBinding
import com.sillyapps.meantime.ui.ItemTouchHelperAdapter
import com.sillyapps.meantime.ui.ItemClickListener

class SchemeAdapter(private val viewModel: SchemeViewModel, private val onClickListener: ItemClickListener? = null): ListAdapter<SchemeTemplate, SchemeAdapter.ViewHolder>(
    SchemeTemplateDiffCallback()
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

    override fun onItemPicked(position: Int) {

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

        fun bind(item: SchemeTemplate, onClickListener: ItemClickListener?) {
            binding.schemeTemplate = item
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

class SchemeTemplateDiffCallback: DiffUtil.ItemCallback<SchemeTemplate>() {
    override fun areItemsTheSame(oldItem: SchemeTemplate, newItem: SchemeTemplate): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: SchemeTemplate, newItem: SchemeTemplate): Boolean {
        return oldItem.templateInfo.id == newItem.templateInfo.id
    }
}