/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:19
 */

package pl.gunock.lyriccast.ui.category_manager

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bson.types.ObjectId
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.ActivityCategoryManagerBinding
import pl.gunock.lyriccast.databinding.ContentCategoryManagerBinding
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.domain.models.CategoryItem
import pl.gunock.lyriccast.extensions.loadAd
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker

class CategoryManagerActivity : AppCompatActivity() {

    private val mDatabaseViewModel: DatabaseViewModel by viewModels {
        DatabaseViewModel.Factory(resources)
    }
    private lateinit var mBinding: ContentCategoryManagerBinding

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
        val rootBinding = ActivityCategoryManagerBinding.inflate(layoutInflater)
        mBinding = rootBinding.contentCategoryManager
        setContentView(rootBinding.root)
        setSupportActionBar(rootBinding.toolbarCategoryManager)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mBinding.advCategoryManager.loadAd()

        // TODO: Possible leak
        mEditCategoryDialogViewModel =
            ViewModelProvider(this).get(EditCategoryDialogViewModel::class.java)

        mDatabaseViewModel.allCategories.addChangeListener { categories ->
            val categoryNames: Set<String> = categories.map { it.name }.toSet()
            mEditCategoryDialogViewModel.categoryNames = categoryNames
        }

        mBinding.rcvCategories.setHasFixedSize(true)
        mBinding.rcvCategories.layoutManager = LinearLayoutManager(baseContext)

        setupCategories()
    }

    override fun onDestroy() {
        mDatabaseViewModel.close()
        super.onDestroy()
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
            mBinding.rcvCategories.context,
            mSelectionTracker = mSelectionTracker
        )
        mBinding.rcvCategories.adapter = mCategoryItemsAdapter

        mDatabaseViewModel.allCategories.addChangeListener { categories ->
            lifecycleScope.launch(Dispatchers.Main) {
                mCategoryItemsAdapter.submitCollection(categories)
            }
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
        val selectedCategoryIds: List<ObjectId> = mCategoryItemsAdapter.categoryItems
            .filter { item -> item.isSelected.value!! }
            .map { items -> items.category.id }

        mDatabaseViewModel.deleteCategories(selectedCategoryIds)
        resetSelection()

        return true
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
        val categoryItem = mCategoryItemsAdapter.categoryItems
            .first { category -> category.isSelected.value!! }

        mEditCategoryDialogViewModel.category = categoryItem.category

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

}