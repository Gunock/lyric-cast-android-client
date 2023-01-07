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
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.ActivityCategoryManagerBinding
import pl.gunock.lyriccast.databinding.ContentCategoryManagerBinding
import pl.gunock.lyriccast.shared.extensions.loadAd
import pl.gunock.lyriccast.ui.category_manager.edit_category.EditCategoryDialogFragment

@AndroidEntryPoint
class CategoryManagerActivity : AppCompatActivity() {

    private val viewModel: CategoryManagerModel by viewModels()

    private lateinit var binding: ContentCategoryManagerBinding

    private lateinit var categoryItemsAdapter: CategoryItemsAdapter

    private var actionMenu: Menu? = null
    private var actionMode: ActionMode? = null
    private val actionModeCallback: ActionMode.Callback = CategoryManagerActionModeCallback()

    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootBinding = ActivityCategoryManagerBinding.inflate(layoutInflater)
        binding = rootBinding.contentCategoryManager
        setContentView(rootBinding.root)
        setSupportActionBar(rootBinding.toolbarCategoryManager)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding.advCategoryManager.loadAd()
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
            R.id.menu_add_category -> showAddCategoryDialog()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        categoryItemsAdapter = CategoryItemsAdapter(
            binding.rcvCategories.context,
            viewModel.selectionTracker
        )
        binding.rcvCategories.adapter = categoryItemsAdapter

        viewModel.categories
            .onEach { categoryItemsAdapter.submitCollection(it) }
            .flowOn(Dispatchers.Main)
            .launchIn(lifecycleScope)

        viewModel.numberOfSelectedCategories
            .onEach(this::onSelectCategory)
            .flowOn(Dispatchers.Main)
            .launchIn(lifecycleScope)

        viewModel.selectedCategoryPosition
            .onEach { categoryItemsAdapter.notifyItemChanged(it) }
            .flowOn(Dispatchers.Default)
            .launchIn(lifecycleScope)
    }

    private fun showAddCategoryDialog(): Boolean {
        val dialogFragment = EditCategoryDialogFragment()
        dialogFragment.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.Theme_LyricCast_Dialog_NoTitle
        )

        dialogFragment.show(supportFragmentManager, EditCategoryDialogFragment.TAG)
        return true
    }

    private fun editSelectedCategory(): Boolean {
        val categoryItem = viewModel.getSelectedCategory()

        val dialogFragment = EditCategoryDialogFragment(categoryItem)
        dialogFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_LyricCast_Dialog_NoTitle)
        dialogFragment.show(supportFragmentManager, EditCategoryDialogFragment.TAG)

        resetSelection()

        return true
    }

    private fun onSelectCategory(numberOfSelectedCategories: Pair<Int, Int>): Boolean {
        val (countBefore: Int, countAfter: Int) = numberOfSelectedCategories

        if ((countBefore == 0 && countAfter == 1) || (countBefore == 1 && countAfter == 0)) {
            categoryItemsAdapter.notifyItemRangeChanged(0, viewModel.categories.value.size)
        }

        when (countAfter) {
            0 -> {
                actionMode?.finish()
                return false
            }
            1 -> {
                if (actionMode == null) {
                    actionMode = startSupportActionMode(actionModeCallback)
                }

                showMenuActions()
            }
            2 -> showMenuActions(showEdit = false)
        }
        return true
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
        viewModel.resetCategorySelection()
        categoryItemsAdapter.notifyItemRangeChanged(0, viewModel.categories.value.size)
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
                    R.id.action_menu_edit -> editSelectedCategory()
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

}