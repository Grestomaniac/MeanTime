package com.sillyapps.meantime.ui.explorer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.sillyapps.meantime.databinding.FragmentExplorerBinding
import com.sillyapps.meantime.ui.explorer.recyclerview.ExplorerAdapter
import com.sillyapps.meantime.ui.mainscreen.recyclerview.RecVClickListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class TemplateExplorerFragment : Fragment() {

    private val viewModel by viewModels<TemplateExplorerViewModel>()

    private lateinit var viewDataBinding: FragmentExplorerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = FragmentExplorerBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
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
        val explorerAdapter = ExplorerAdapter(RecVClickListener { id ->
            Timber.d("Navigating to position $id")
            navigateToEditTemplateFragment(id)
        })
        viewDataBinding.items.adapter = explorerAdapter

        viewModel.items.observe(viewLifecycleOwner, {
            it.let { explorerAdapter.submitList(it) }
        })
    }

    private fun navigateToEditTemplateFragment(id: Int = 0) {
        findNavController().navigate(TemplateExplorerFragmentDirections.actionExplorerFragmentToEditTemplateGraph(id))
    }

}