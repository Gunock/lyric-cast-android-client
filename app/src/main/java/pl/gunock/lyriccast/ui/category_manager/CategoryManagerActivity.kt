/*
 * Created by Tomasz Kiljanczyk on 07/01/2023, 21:51
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 07/01/2023, 20:15
 */

package pl.gunock.lyriccast.ui.category_manager

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.datastore.core.DataStore
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.application.AppSettings
import pl.gunock.lyriccast.databinding.ActivityCategoryManagerBinding
import pl.gunock.lyriccast.databinding.ContentCategoryManagerBinding
import pl.gunock.lyriccast.shared.extensions.loadAd
import pl.gunock.lyriccast.ui.category_manager.edit_category.EditCategoryDialogFragment
import pl.gunock.lyriccast.ui.shared.selection.MappedItemKeyProvider
import pl.gunock.lyriccast.ui.shared.selection.SimpleItemDetailsLookup
import javax.inject.Inject

@AndroidEntryPoint
class CategoryManagerActivity : AppCompatActivity() {

    private val viewModel: CategoryManagerModel by viewModels()

    @Inject
    lateinit var dataStore: DataStore<AppSettings>

    private lateinit var binding: ContentCategoryManagerBinding

    private lateinit var categoryItemsAdapter: CategoryItemsAdapter

    private var actionMenu: Menu? = null
    private var actionMode: ActionMode? = null
    private val actionModeCallback: ActionMode.Callback = CategoryManagerActionModeCallback()

    private var onBackPressedCallback: OnBackPressedCallback? = null

    private lateinit var tracker: SelectionTracker<Long>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = getColor(R.color.background_1)

        val rootBinding = ActivityCategoryManagerBinding.inflate(layoutInflater)
        binding = rootBinding.contentCategoryManager
        setContentView(rootBinding.root)
        setSupportActionBar(rootBinding.toolbarCategoryManager)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        CoroutineScope(Dispatchers.Main).launch {
            binding.advCategoryManager.loadAd(dataStore)
        }
        binding.rcvCategories.setHasFixedSize(true)
        binding.rcvCategories.layoutManager = LinearLayoutManager(baseContext)

        setupRecyclerView()
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