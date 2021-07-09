package com.sillyapps.meantime.ui.edittemplatescreen

import android.os.Bundle
import android.view.*
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import com.sillyapps.meantime.R
import com.sillyapps.meantime.databinding.FragmentEditTaskBinding
import com.sillyapps.meantime.databinding.FragmentTaskChooserBinding
import com.sillyapps.meantime.setupToolbar
import com.sillyapps.meantime.ui.ItemClickListener
import com.sillyapps.meantime.utils.tintMenuIcons
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskChooserFragment: Fragment() {

    private val viewModel: EditTemplateViewModel by navGraphViewModels(R.id.edit_template_graph) {
        defaultViewModelProviderFactory
    }

    private lateinit var binding: FragmentTaskChooserBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskChooserBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this.viewLifecycleOwner

        setupToolbar(binding.toolbar)
        setupAdapter()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.task_chooser_menu, menu)
        tintMenuIcons(menu.children, requireContext())
    }

    private fun setupAdapter() {
        val clickListener = object : ItemClickListener {
            override fun onClickItem(index: Int) {

            }

            override fun onLongClick(index: Int): Boolean {
                return true
            }
        }

        val adapter = TaskChooserAdapter(clickListener)
    }

}