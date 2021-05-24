package com.sillyapps.meantime.ui.edittemplatescreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import com.sillyapps.meantime.R
import com.sillyapps.meantime.databinding.FragmentEditTaskBinding
import com.sillyapps.meantime.databinding.FragmentTaskChooserBinding
import com.sillyapps.meantime.setupToolbar

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
        binding.task = viewModel.task.value

        setupToolbar(binding.toolbar)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this.viewLifecycleOwner


    }

}