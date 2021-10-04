/*
 * Created by Tomasz Kiljanczyk on 04/10/2021, 18:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 04/10/2021, 18:29
 */

package pl.gunock.lyriccast.ui.category_manager

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.ActivityCategoryManagerBinding
import pl.gunock.lyriccast.databinding.ContentCategoryManagerBinding
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.domain.models.CategoryItem
import pl.gunock.lyriccast.shared.extensions.loadAd
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker
import javax.inject.Inject

@AndroidEntryPoint
class CategoryManagerActivity : AppCompatActivity() {

    @Inject
    lateinit var categoriesRepository: CategoriesRepository

    private lateinit var mBinding: ContentCategoryManagerBinding

    private lateinit var mEditCategoryDialogViewModel: EditCategoryDialogViewModel

    private lateinit var mCategoryItemsAdapter: CategoryItemsAdapter
    private lateinit var mSelectionTracker: SelectionTracker<BaseViewHolder>

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
            lifecycleScope.launch(Dispatchers.Main) {
                val result = when (item.itemId) {
                    R.id.action_menu_delete -> deleteSelectedCategories()
                    R.id.action_menu_edit -> editSelectedCategory()
                    else -> false
                }

                if (result) {
                    mode.finish()
                }
            }

            return true
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

        mBinding.rcvCategories.setHasFixedSize(true)
        mBinding.rcvCategories.layoutManager = LinearLayoutManager(baseContext)

        setupCategories()
    }

    private var mCategoriesSubscription: Disposable? = null

    override fun onResume() {
        super.onResume()

        mCategoriesSubscription =
            categoriesRepository.getAllCategories().subscribe { categories ->
                lifecycleScope.launch(Dispatchers.Default) {
                    mCategoryItemsAdapter.submitCollection(categories)

                    val categoryNames: Set<String> = categories.map { it.name }.toSet()
                    mEditCategoryDialogViewModel.categoryNames = categoryNames
                }
            }
    }

    override fun onPause() {
        super.onPause()

        mCategoriesSubscription?.dispose()
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
    }

    private fun onCategoryClick(
        @Suppress("UNUSED_PARAMETER")
        holder: BaseViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        val item = mCategoryItemsAdapter.categoryItems[position]

        if (isLongClick || mSelectionTracker.count != 0) {
            return selectCategory(item)
        }

        return false
    }

    private suspend fun deleteSelectedCategories(): Boolean {
        val selectedCategoryIds: List<String> = mCategoryItemsAdapter.categoryItems
            .filter { item -> item.isSelected.value!! }
            .map { items -> items.category.id }

        categoriesRepository.deleteCategories(selectedCategoryIds)
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
        item.isSelected.postValue(!item.isSelected.value!!)

        when (mSelectionTracker.countAfter) {
            0 -> {
                mActionMode?.finish()
                return false
            }
            1 -> {
                if (!mCategoryItemsAdapter.showCheckBox.value!!) {
                    mCategoryItemsAdapter.showCheckBox.postValue(true)
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
            mCategoryItemsAdapter.showCheckBox.postValue(false)
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