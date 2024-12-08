/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.ui.category_manager

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.databinding.ActivityCategoryManagerBinding
import dev.thomas_kiljanczyk.lyriccast.databinding.ContentCategoryManagerBinding
import dev.thomas_kiljanczyk.lyriccast.ui.category_manager.edit_category.EditCategoryDialogFragment
import dev.thomas_kiljanczyk.lyriccast.ui.shared.selection.MappedItemKeyProvider
import dev.thomas_kiljanczyk.lyriccast.ui.shared.selection.SimpleItemDetailsLookup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class CategoryManagerActivity : AppCompatActivity() {

    private val viewModel: CategoryManagerModel by viewModels()

    private lateinit var binding: ContentCategoryManagerBinding

    private lateinit var categoryItemsAdapter: CategoryItemsAdapter

    private var actionMenu: Menu? = null
    private var actionMode: ActionMode? = null
    private val actionModeCallback: ActionMode.Callback = CategoryManagerActionModeCallback()

    private var onBackPressedCallback: OnBackPressedCallback? = null

    private lateinit var tracker: SelectionTracker<Long>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val rootBinding = ActivityCategoryManagerBinding.inflate(layoutInflater)
        binding = rootBinding.contentCategoryManager
        setContentView(rootBinding.root)
        setSupportActionBar(rootBinding.toolbarCategoryManager)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding.rcvCategories.setHasFixedSize(true)
        binding.rcvCategories.layoutManager = LinearLayoutManager(baseContext)

        setupRecyclerView()

        setOnApplyWindowInsetsListener(rootBinding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            rootBinding.toolbarCategoryManager.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
                bottomMargin = 0
            }

            binding.rcvCategories.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom
            }

            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_category_manager, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_add_category -> {
                showAddCategoryDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        categoryItemsAdapter = CategoryItemsAdapter(binding.rcvCategories.context)
        binding.rcvCategories.adapter = categoryItemsAdapter

        tracker = SelectionTracker.Builder(
            "selection",
            binding.rcvCategories,
            MappedItemKeyProvider(binding.rcvCategories),
            SimpleItemDetailsLookup(binding.rcvCategories),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        tracker.addObserver(CategorySelectionObserver())

        viewModel.categories
            .onEach { categoryItemsAdapter.submitList(it) }
            .launchIn(lifecycleScope)
    }

    private fun showAddCategoryDialog() {
        EditCategoryDialogFragment().show(supportFragmentManager, EditCategoryDialogFragment.TAG)
    }

    private fun editSelectedCategory() {
        val categoryItem = categoryItemsAdapter.currentList
            .first { tracker.isSelected(it.category.idLong) }

        EditCategoryDialogFragment(categoryItem)
            .show(supportFragmentManager, EditCategoryDialogFragment.TAG)
    }

    private fun onSelectCategory() {
        when (tracker.selection.size()) {
            0 -> {
                actionMode?.finish()
            }

            1 -> {
                if (actionMode == null) {
                    actionMode = startSupportActionMode(actionModeCallback)
                    viewModel.showSelectionCheckboxes()
                    notifyAllItemsChanged()
                }

                showMenuActions()
            }

            2 -> showMenuActions(showEdit = false)
        }
    }

    private fun showMenuActions(
        showDelete: Boolean = true,
        showEdit: Boolean = true
    ) {
        actionMenu?.apply {
            findItem(R.id.action_menu_delete).isVisible = showDelete
            findItem(R.id.action_menu_edit).isVisible = showEdit
        }
    }

    private fun resetSelection() {
        tracker.clearSelection()
        viewModel.hideSelectionCheckboxes()
        notifyAllItemsChanged()
    }

    private fun notifyAllItemsChanged() {
        categoryItemsAdapter.notifyItemRangeChanged(0, viewModel.categories.value.size, true)
    }

    private inner class CategoryManagerActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.action_menu_category_manager, menu)
            mode.title = ""
            actionMenu = menu

            onBackPressedCallback =
                onBackPressedDispatcher.addCallback(this@CategoryManagerActivity) {
                    resetSelection()
                    onBackPressedCallback?.remove()
                    onBackPressedCallback = null
                }

            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            showMenuActions(showDelete = false, showEdit = false)
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            lifecycleScope.launch(Dispatchers.Default) {
                val result = when (item.itemId) {
                    R.id.action_menu_delete -> {
                        viewModel.deleteSelectedCategories()
                        true
                    }

                    R.id.action_menu_edit -> {
                        editSelectedCategory()
                        true
                    }

                    else -> false
                }

                if (result) {
                    withContext(Dispatchers.Main) { mode.finish() }
                }
            }

            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            actionMenu = null
            resetSelection()

            onBackPressedCallback?.remove()
            onBackPressedCallback = null
        }
    }


    private inner class CategorySelectionObserver : SelectionTracker.SelectionObserver<Long>() {
        override fun onItemStateChanged(key: Long, selected: Boolean) {
            super.onItemStateChanged(key, selected)
            if (viewModel.selectCategory(key, selected)) {
                onSelectCategory()
            }
        }
    }
}