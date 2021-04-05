/*
 * Created by Tomasz Kiljanczyk on 4/5/21 4:34 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 4:07 PM
 */

package pl.gunock.lyriccast.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.runBlocking
import pl.gunock.lyriccast.LyricCastApplication
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.adapters.CategoryItemsAdapter
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.datamodel.DatabaseViewModelFactory
import pl.gunock.lyriccast.datamodel.entities.Category
import pl.gunock.lyriccast.fragments.dialogs.EditCategoryDialogFragment
import pl.gunock.lyriccast.fragments.viewholders.EditCategoryDialogViewModel
import pl.gunock.lyriccast.misc.SelectionTracker
import pl.gunock.lyriccast.models.CategoryItem

class CategoryManagerActivity : AppCompatActivity() {

    private val databaseViewModel: DatabaseViewModel by viewModels {
        DatabaseViewModelFactory(baseContext, (application as LyricCastApplication).repository)
    }

    private lateinit var categoryItemsRecyclerView: RecyclerView

    private lateinit var editCategoryDialogViewModel: EditCategoryDialogViewModel

    private lateinit var categoryItemsAdapter: CategoryItemsAdapter
    private lateinit var selectionTracker: SelectionTracker<CategoryItemsAdapter.ViewHolder>

    private var actionMenu: Menu? = null
    private var actionMode: ActionMode? = null
    private val actionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.action_menu_category_manager, menu)
            mode.title = ""
            actionMenu = menu
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            showMenuActions(showDelete = false, showEdit = false)
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val result = when (item.itemId) {
                R.id.action_menu_delete -> deleteSelectedCategories()
                R.id.action_menu_edit -> editSelectedCategory()
                else -> false
            }

            if (result) {
                mode.finish()
            }

            return result
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            actionMenu = null
            resetSelection()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_category_manager)
        setSupportActionBar(findViewById(R.id.toolbar_category_manager))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // TODO: Possible leak
        editCategoryDialogViewModel =
            ViewModelProvider(this).get(EditCategoryDialogViewModel::class.java)

        databaseViewModel.allCategories.observe(this) { categories ->
            editCategoryDialogViewModel.categoryNames.value =
                categories.map { category -> category.name }.toSet()
        }

        categoryItemsRecyclerView = findViewById(R.id.rcv_categories)
        categoryItemsRecyclerView.setHasFixedSize(true)
        categoryItemsRecyclerView.layoutManager = LinearLayoutManager(baseContext)

        setupCategories()
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

    override fun onBackPressed() {
        if (selectionTracker.count != 0) {
            categoryItemsAdapter.resetSelection()
            resetSelection()
        } else {
            super.onBackPressed()
        }
    }

    private fun setupCategories() {
        selectionTracker = SelectionTracker(categoryItemsRecyclerView, this::onCategoryClick)

        categoryItemsAdapter = CategoryItemsAdapter(
            categoryItemsRecyclerView.context,
            selectionTracker = selectionTracker
        )
        categoryItemsRecyclerView.adapter = categoryItemsAdapter

        databaseViewModel.allCategories.observe(this) { categories ->
            categoryItemsAdapter.submitCollection(categories)
        }
    }

    private fun onCategoryClick(
        @Suppress("UNUSED_PARAMETER")
        holder: CategoryItemsAdapter.ViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        val item = categoryItemsAdapter.categoryItems[position]

        if (isLongClick || selectionTracker.count != 0) {
            return selectCategory(item)
        }

        return false
    }

    private fun deleteSelectedCategories(): Boolean {
        val selectedCategories = categoryItemsAdapter.categoryItems
            .filter { item -> item.isSelected.value!! }
            .map { items -> items.category.id }

        runBlocking { databaseViewModel.deleteCategories(selectedCategories) }

        resetSelection()

        return true
    }

    private fun showAddCategoryDialog(): Boolean {
        editCategoryDialogViewModel.category.observe(this, this::observeViewModelCategory)
        val dialogFragment = EditCategoryDialogFragment()
        dialogFragment.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.Theme_LyricCast_Light_Dialog
        )

        dialogFragment.show(supportFragmentManager, EditCategoryDialogFragment.TAG)
        return true
    }

    private fun editSelectedCategory(): Boolean {
        val categoryItem = categoryItemsAdapter.categoryItems
            .first { category -> category.isSelected.value!! }

        editCategoryDialogViewModel.category.observe(this, this::observeViewModelCategory)

        val dialogFragment = EditCategoryDialogFragment(categoryItem)
        dialogFragment.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.Theme_LyricCast_Light_Dialog
        )

        dialogFragment.show(supportFragmentManager, EditCategoryDialogFragment.TAG)
        resetSelection()

        return true
    }

    private fun selectCategory(item: CategoryItem): Boolean {
        item.isSelected.value = !item.isSelected.value!!

        when (selectionTracker.countAfter) {
            0 -> {
                actionMode?.finish()
                return false
            }
            1 -> {
                if (!categoryItemsAdapter.showCheckBox.value!!) {
                    categoryItemsAdapter.showCheckBox.value = true
                }

                if (actionMode == null) {
                    actionMode = startSupportActionMode(actionModeCallback)
                }

                showMenuActions()
            }
            2 -> showMenuActions(showEdit = false)
        }

        return true
    }

    private fun resetSelection() {
        if (categoryItemsAdapter.showCheckBox.value!!) {
            categoryItemsAdapter.showCheckBox.value = false
        }
        categoryItemsAdapter.resetSelection()
    }

    private fun showMenuActions(
        showDelete: Boolean = true,
        showEdit: Boolean = true
    ) {
        actionMenu ?: return
        actionMenu!!.findItem(R.id.action_menu_delete).isVisible = showDelete
        actionMenu!!.findItem(R.id.action_menu_edit).isVisible = showEdit
    }

    private fun observeViewModelCategory(viewModelCategory: Category?) {
        if (viewModelCategory == null) {
            return
        }

        databaseViewModel.upsertCategory(editCategoryDialogViewModel.category.value!!)

        editCategoryDialogViewModel.category.removeObservers(this)
        editCategoryDialogViewModel.category.value = null
    }
}