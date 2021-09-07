package com.sillyapps.meantime.ui.edittemplatescreen.taskchooser

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.sillyapps.meantime.data.SimpleBaseTask
import com.sillyapps.meantime.databinding.DialogTaskChooserBinding
import com.sillyapps.meantime.ui.CustomSearchView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskChooserDialog(private val onObtainResult: OnObtainResult): DialogFragment() {

    private val viewModel: TaskChooserViewModel by viewModels(ownerProducer = { this })

    private lateinit var binding: DialogTaskChooserBinding
    private lateinit var adapter: TaskChooserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogTaskChooserBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this.viewLifecycleOwner

        setupDialogSize()
        setupAdapter()
        setupSearchView()
    }

    private fun setupAdapter() {
        val clickListener = TaskChooserAdapter.ViewHolder.OnClick { onObtainResult.obtainResult(it) }

        adapter = TaskChooserAdapter(clickListener)

        viewModel.baseTasks.observe(viewLifecycleOwner) {
            adapter.submitUnfilteredList(it)
        }

        binding.baseTasks.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.textChangedListener = CustomSearchView.TextChangedListener {
            adapter.filter(it)
        }
    }

    private fun setupDialogSize() {
        val width = (resources.displayMetrics.widthPixels*0.9).toInt()
        val height = (resources.displayMetrics.heightPixels*0.9).toInt()

        dialog?.window?.setLayout(width, height)
    }

    fun interface OnObtainResult {
        fun obtainResult(baseTask: SimpleBaseTask)
    }
}