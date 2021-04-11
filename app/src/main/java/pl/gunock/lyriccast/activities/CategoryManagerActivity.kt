/*
 * Created by Tomasz Kiljanczyk on 4/11/21 2:05 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/11/21 12:12 AM
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

    private val mDatabaseViewModel: DatabaseViewModel by viewModels {
        DatabaseViewModelFactory(
            baseContext.resources,
            (application as LyricCastApplication).repository
        )
    }

    private lateinit var mCategoryItemsRecyclerView: RecyclerView

    private lateinit var mEditCategoryDialogViewModel: EditCategoryDialogViewModel

    private lateinit var mCategoryItemsAdapter: CategoryItemsAdapter
    private lateinit var mSelectionTracker: SelectionTracker<CategoryItemsAdapter.ViewHolder>

    private var mActionMenu: Menu? = null
    private var mActionMode: ActionMode? = null
    private val mActionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.action_menu_category_manager, menu)
            mode.title = ""
            mActionMenu = menu
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
            mActionMode = null
            mActionMenu = null
            resetSelection()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_category_manager)
        setSupportActionBar(findViewById(R.id.toolbar_category_manager))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // TODO: Possible leak
        mEditCategoryDialogViewModel =
            ViewModelProvider(this).get(EditCategoryDialogViewModel::class.java)

        mDatabaseViewModel.allCategories.observe(this) { categories ->
            mEditCategoryDialogViewModel.categoryNames.value =
                categories.map { category -> category.name }.toSet()
        }

        mCategoryItemsRecyclerView = findViewById(R.id.rcv_categories)
        mCategoryItemsRecyclerView.setHasFixedSize(true)
        mCategoryItemsRecyclerView.layoutManager = LinearLayoutManager(baseContext)

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
        if (mSelectionTracker.count != 0) {
            mCategoryItemsAdapter.resetSelection()
            resetSelection()
        } else {
            super.onBackPressed()
        }
    }

    private fun setupCategories() {
        mSelectionTracker = SelectionTracker(this::onCategoryClick)

        mCategoryItemsAdapter = CategoryItemsAdapter(
            mCategoryItemsRecyclerView.context,
            mSelectionTracker = mSelectionTracker
        )
        mCategoryItemsRecyclerView.adapter = mCategoryItemsAdapter

        mDatabaseViewModel.allCategories.observe(this) { categories ->
            mCategoryItemsAdapter.submitCollection(categories)
        }
    }

    private fun onCategoryClick(
        @Suppress("UNUSED_PARAMETER")
        holder: CategoryItemsAdapter.ViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        val item = mCategoryItemsAdapter.categoryItems[position]

        if (isLongClick || mSelectionTracker.count != 0) {
            return selectCategory(item)
        }

        return false
    }

    private fun deleteSelectedCategories(): Boolean {
        val selectedCategories = mCategoryItemsAdapter.categoryItems
            .filter { item -> item.isSelected.value!! }
            .map { items -> items.category.id }

        runBlocking { mDatabaseViewModel.deleteCategories(selectedCategories) }

        resetSelection()

        return true
    }

    private fun showAddCategoryDialog(): Boolean {
        mEditCategoryDialogViewModel.category.observe(this, this::observeViewModelCategory)
        val dialogFragment = EditCategoryDialogFragment()
        dialogFragment.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.Theme_LyricCast_Dialog_NoTitle
        )

        dialogFragment.show(supportFragmentManager, EditCategoryDialogFragment.TAG)
        return true
    }

    private fun editSelectedCategory(): Boolean {
        val categoryItem = mCategoryItemsAdapter.categoryItems
            .first { category -> category.isSelected.value!! }

        mEditCategoryDialogViewModel.category.observe(this, this::observeViewModelCategory)

        val dialogFragment = EditCategoryDialogFragment(categoryItem)
        dialogFragment.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.Theme_LyricCast_Dialog_NoTitle
        )

        dialogFragment.show(supportFragmentManager, EditCategoryDialogFragment.TAG)
        resetSelection()

        return true
    }

    private fun selectCategory(item: CategoryItem): Boolean {
        item.isSelected.value = !item.isSelected.value!!

        when (mSelectionTracker.countAfter) {
            0 -> {
                mActionMode?.finish()
                return false
            }
            1 -> {
                if (!mCategoryItemsAdapter.showCheckBox.value!!) {
                    mCategoryItemsAdapter.showCheckBox.value = true
                }

                if (mActionMode == null) {
                    mActionMode = startSupportActionMode(mActionModeCallback)
                }

                showMenuActions()
            }
            2 -> showMenuActions(showEdit = false)
        }

        return true
    }

    private fun resetSelection() {
        if (mCategoryItemsAdapter.showCheckBox.value!!) {
            mCategoryItemsAdapter.showCheckBox.value = false
        }
        mCategoryItemsAdapter.resetSelection()
    }

    private fun showMenuActions(
        showDelete: Boolean = true,
        showEdit: Boolean = true
    ) {
        mActionMenu ?: return
        mActionMenu!!.findItem(R.id.action_menu_delete).isVisible = showDelete
        mActionMenu!!.findItem(R.id.action_menu_edit).isVisible = showEdit
    }

    private fun observeViewModelCategory(viewModelCategory: Category?) {
        if (viewModelCategory == null) {
            return
        }

        mDatabaseViewModel.upsertCategory(mEditCategoryDialogViewModel.category.value!!)

        mEditCategoryDialogViewModel.category.removeObservers(this)
        mEditCategoryDialogViewModel.category.value = null
    }
}