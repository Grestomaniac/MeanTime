package com.sillyapps.meantime.ui.schemescreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import com.sillyapps.meantime.databinding.FragmentSchemeBinding
import com.sillyapps.meantime.ui.ItemTouchHelperCallback
import com.sillyapps.meantime.ui.mainscreen.recyclerview.ItemTouchHelperOnDetachedCallback
import com.sillyapps.meantime.ui.mainscreen.recyclerview.RunningTasksAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SchemeFragment: Fragment() {

    private val viewModel: SchemeViewModel by viewModels()

    private lateinit var binding: FragmentSchemeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSchemeBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        setupRecyclerView()
        binding.addTemplateFab.setOnClickListener { pickTemplate() }
    }

    private fun setupRecyclerView() {
        val adapter = SchemeAdapter(viewModel)
        binding.items.adapter = adapter

        val itemTouchHelperCallback = ItemTouchHelperCallback(adapter)
        val touchHelper = ItemTouchHelper(itemTouchHelperCallback)
        touchHelper.attachToRecyclerView(binding.items)

        viewModel.schemeTemplates.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Int>(KEY_TEMPLATE_ID)?.observe(viewLifecycleOwner) {
            viewModel.addTemplate(it)
            adapter.notifyItemInserted(adapter.currentList.lastIndex)
        }
    }

    private fun pickTemplate() {
        findNavController().navigate(SchemeFragmentDirections.actionSchemeFragmentToExplorerFragment())
    }

    companion object {
        const val KEY_TEMPLATE_ID = "template_id"
    }
}