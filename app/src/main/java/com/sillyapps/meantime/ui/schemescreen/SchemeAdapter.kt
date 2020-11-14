package com.sillyapps.meantime.ui.schemescreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sillyapps.meantime.data.Task
import com.sillyapps.meantime.data.Template
import com.sillyapps.meantime.databinding.ItemEditorTaskBinding
import com.sillyapps.meantime.databinding.ItemShemeTemplateBinding
import com.sillyapps.meantime.ui.ItemTouchHelperAdapter
import com.sillyapps.meantime.ui.ItemClickListener
import com.sillyapps.meantime.ui.explorer.recyclerview.TemplatesDiffCallback

class SchemeAdapter(private val viewModel: SchemeViewModel, private val onClickListener: ItemClickListener? = null): ListAdapter<Template, SchemeAdapter.ViewHolder>(
    TemplatesDiffCallback()
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

    override fun onItemSwiped(position: Int) {
        viewModel.notifyItemRemoved(position)
        notifyItemRemoved(position)
    }

    class ViewHolder private constructor(private val binding: ItemShemeTemplateBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Template, onClickListener: ItemClickListener?) {
            binding.template = item
            binding.clickListener = onClickListener
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding = ItemShemeTemplateBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}