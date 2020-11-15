package com.sillyapps.meantime.ui.explorer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import com.sillyapps.meantime.databinding.FragmentExplorerBinding
import com.sillyapps.meantime.ui.ItemTouchHelperCallbackNoDrag
import com.sillyapps.meantime.ui.explorer.recyclerview.ExplorerAdapter
import com.sillyapps.meantime.ui.ItemClickListener
import com.sillyapps.meantime.ui.schemescreen.SchemeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TemplateExplorerFragment : Fragment() {

    private val viewModel by viewModels<TemplateExplorerViewModel>()
    private val args: TemplateExplorerFragmentArgs by navArgs()

    private lateinit var viewDataBinding: FragmentExplorerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = FragmentExplorerBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
            editMode = args.editMode
        }
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner

        setupAdapter()
        setupFloatingButton()
    }

    private fun setupFloatingButton() {
        viewDataBinding.addTemplateFab.setOnClickListener {
            navigateToEditTemplateFragment()
        }
    }

    private fun setupAdapter() {
        val clickListener = if (args.editMode) getEditModeClickListener()
                            else getExplorerModeClickListener()

        val explorerAdapter = ExplorerAdapter(viewModel, clickListener)
        viewDataBinding.items.adapter = explorerAdapter

        val itemTouchHelperCallback = ItemTouchHelperCallbackNoDrag(explorerAdapter)
        val touchHelper = ItemTouchHelper(itemTouchHelperCallback)
        touchHelper.attachToRecyclerView(viewDataBinding.items)

        viewModel.items.observe(viewLifecycleOwner, {
            it.let { explorerAdapter.submitList(it) }
        })
    }

    private fun getEditModeClickListener(): ItemClickListener {
        return object : ItemClickListener {
            override fun onClickItem(index: Int) {
                navigateToEditTemplateFragment(index)
            }

            override fun onLongClick(index: Int): Boolean {
                viewModel.selectDefaultTemplate(index)
                return true
            }

        }
    }

    private fun getExplorerModeClickListener(): ItemClickListener {
        return object : ItemClickListener {
            override fun onClickItem(index: Int) {
                returnResultToSchemeFragment(index)
            }

            override fun onLongClick(index: Int): Boolean {
                viewModel.selectDefaultTemplate(index)
                return true
            }
        }
    }

    private fun returnResultToSchemeFragment(position: Int) {
        findNavController().apply {
            val template = viewModel.getSimplifiedTemplate(position)
            findNavController().previousBackStackEntry?.savedStateHandle?.set(SchemeFragment.TEMPLATE_KEY, template)
            popBackStack()
        }
    }

    private fun navigateToEditTemplateFragment(id: Int = 0) {
        findNavController().navigate(TemplateExplorerFragmentDirections.actionExplorerFragmentToEditTemplateGraph(id))
    }

}