package com.sillyapps.meantime.ui.schemescreen

import android.os.Bundle
import android.view.*
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.sillyapps.meantime.R
import com.sillyapps.meantime.data.SimplifiedTemplate
import com.sillyapps.meantime.databinding.FragmentSchemeBinding
import com.sillyapps.meantime.tintMenuIcons
import com.sillyapps.meantime.ui.ItemTouchHelperCallback
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

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
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        setupRecyclerView()
        binding.addTemplateFab.setOnClickListener { pickTemplate() }

        findNavController().currentBackStackEntry?.savedStateHandle?.let {
            it.getLiveData<SimplifiedTemplate>(TEMPLATE_KEY).observe(
                viewLifecycleOwner) { result ->
                viewModel.addTemplate(result)
                it.remove<SimplifiedTemplate>(TEMPLATE_KEY)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.scheme_menu, menu)
        tintMenuIcons(menu.children, requireContext())
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_save -> {
                onSaveButtonClick()
                true
            }
            else -> false
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

    }

    private fun onSaveButtonClick() {
        viewModel.saveScheme()
    }

    private fun pickTemplate() {
        findNavController().navigate(SchemeFragmentDirections.actionSchemeFragmentToExplorerFragment())
    }

    companion object {
        const val TEMPLATE_KEY = "template_id"
    }
}