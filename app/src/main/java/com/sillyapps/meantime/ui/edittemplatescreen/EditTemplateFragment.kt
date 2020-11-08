package com.sillyapps.meantime.ui.edittemplatescreen

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.sillyapps.meantime.R
import com.sillyapps.meantime.databinding.FragmentEditTemplateBinding
import com.sillyapps.meantime.ui.edittemplatescreen.recyclerview.TemplateEditorAdapter
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import com.sillyapps.meantime.ui.ItemTouchHelperCallback
import com.sillyapps.meantime.ui.ItemClickListener

@AndroidEntryPoint
class EditTemplateFragment : Fragment() {

    private val viewModel: EditTemplateViewModel by navGraphViewModels(R.id.edit_template_graph) {
        defaultViewModelProviderFactory
    }

    private lateinit var viewDataBinding: FragmentEditTemplateBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = FragmentEditTemplateBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        setHasOptionsMenu(true)
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner

        setupAdapter()
        viewDataBinding.addTemplateFab.setOnClickListener { onAddTaskButtonClick() }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_template_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_save -> {
                onSaveButtonClick()
                true
            }
            else -> false
        }

    private fun setupAdapter() {
        val clickListener = object : ItemClickListener {
            override fun onClickItem(index: Int) {
                editTask(index)
            }

            override fun onLongClick(index: Int): Boolean {
                return true
            }
        }

        val templateEditorAdapter = TemplateEditorAdapter(viewModel, clickListener)
        viewDataBinding.tasks.adapter = templateEditorAdapter

        val itemTouchHelperCallback = ItemTouchHelperCallback(templateEditorAdapter)
        val touchHelper = ItemTouchHelper(itemTouchHelperCallback)
        touchHelper.attachToRecyclerView(viewDataBinding.tasks)

        viewModel.tasks.observe(viewLifecycleOwner, {
            templateEditorAdapter.submitList(it)
        })
    }

    private fun onAddTaskButtonClick() {
        viewModel.createNewTask()
        navigateToEditTaskFragment()
    }

    private fun editTask(position: Int) {
        viewModel.editTask(position)

        navigateToEditTaskFragment()
    }

    private fun navigateToEditTaskFragment() {
        findNavController().navigate(EditTemplateFragmentDirections.actionEditTemplateFragmentToEditTaskFragment())
    }

    private fun onSaveButtonClick() {
        val result = viewModel.saveTemplate()
        if (result.success) showInfoToUser(R.string.template_saved)
        else showInfoToUser(result.messageId)
    }

    private fun showInfoToUser(messageId: Int) {
        val message = getString(messageId)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}