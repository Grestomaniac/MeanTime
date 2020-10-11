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
import com.sillyapps.meantime.ui.edittemplatescreen.recyclerview.ItemTouchHelperAdapter
import com.sillyapps.meantime.ui.edittemplatescreen.recyclerview.ItemTouchHelperCallback
import timber.log.Timber
import java.text.FieldPosition

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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
        /*viewModel.populateTasks()*/
        val templateEditorAdapter = TemplateEditorAdapter()
        viewDataBinding.tasks.adapter = templateEditorAdapter

        val helperAdapter = createHelperAdapter(templateEditorAdapter)
        val itemTouchHelperCallback = ItemTouchHelperCallback(helperAdapter)
        val touchHelper = ItemTouchHelper(itemTouchHelperCallback)
        touchHelper.attachToRecyclerView(viewDataBinding.tasks)

        viewModel.tasks.observe(viewLifecycleOwner, {
            templateEditorAdapter.submitList(it)
        })
    }

    private fun createHelperAdapter(recViewAdapter: TemplateEditorAdapter): ItemTouchHelperAdapter = object : ItemTouchHelperAdapter {
        override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {

            if (fromPosition > toPosition) {
                viewModel.notifyTasksSwapped(toPosition, fromPosition)
            }
            else {
                viewModel.notifyTasksSwapped(fromPosition, toPosition)
            }

            recViewAdapter.notifyItemMoved(fromPosition, toPosition)
            return true
        }

        override fun onItemDropped(toPosition: Int) {
            viewModel.recalculateStartTimes(toPosition)
            recViewAdapter.notifyItemRangeChanged(toPosition, recViewAdapter.itemCount-toPosition)
        }

        override fun onItemDismiss(position: Int) {
            viewModel.notifyTaskRemoved(position)
            recViewAdapter.notifyItemRemoved(position)
            recViewAdapter.notifyItemRangeChanged(position, recViewAdapter.itemCount-position)
        }

        override fun onItemEdit(position: Int) {
            editTask(position)
        }
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
        if (!result.success) showInfoToUser(result.messageId)
    }

    private fun showInfoToUser(messageId: Int) {
        val message = getString(messageId)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}