/*
 * Created by Tomasz Kiljańczyk on 3/15/21 1:22 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/13/21 4:46 PM
 */

package pl.gunock.lyriccast.activities

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.CategoriesContext
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.adapters.CategoryItemsAdapter
import pl.gunock.lyriccast.fragments.dialog.EditCategoryDialogFragment
import pl.gunock.lyriccast.listeners.ClickAdapterItemListener
import pl.gunock.lyriccast.listeners.LongClickAdapterItemListener
import pl.gunock.lyriccast.models.CategoryItem
import pl.gunock.lyriccast.viewmodels.EditCategoryViewModel
import java.util.*

class CategoryManagerActivity : AppCompatActivity() {

    private lateinit var menu: Menu
    private lateinit var categoryItemsRecyclerView: RecyclerView

    private lateinit var viewModel: EditCategoryViewModel

    private var categoryItems: Set<CategoryItem> = setOf()
    private lateinit var categoryItemsAdapter: CategoryItemsAdapter

    private var selectionCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_category_manager)
        setSupportActionBar(findViewById(R.id.toolbar_category_manager))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // TODO: Possible leak
        viewModel = ViewModelProvider(this).get(EditCategoryViewModel::class.java)
        viewModel.category.observe(this, this::observeViewModelCategory)

        categoryItemsRecyclerView = findViewById(R.id.rcv_categories)

        with(categoryItemsRecyclerView) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(baseContext)
        }

        setupCategories()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.menu_category_manager, menu)

        showMenuActions(showDelete = false, showEdit = false)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete -> deleteSelectedCategories()
            R.id.menu_edit -> editSelectedCategory()
            R.id.menu_category_manager -> showAddCategoryDialog()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupCategories() {


        val onLongClickListener =
            LongClickAdapterItemListener { holder: CategoryItemsAdapter.ViewHolder, position: Int, _ ->
                val item = categoryItemsAdapter.categoryItems[position]
                selectCategory(item, holder)
                return@LongClickAdapterItemListener true
            }

        val onClickListener =
            ClickAdapterItemListener { holder: CategoryItemsAdapter.ViewHolder, position: Int, _ ->
                val item = categoryItemsAdapter.categoryItems[position]
                if (selectionCount != 0) {
                    categoryItemsRecyclerView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    selectCategory(item, holder)
                }
            }

        categoryItems = CategoriesContext.getCategoryItems()

        categoryItemsAdapter = CategoryItemsAdapter(
            categoryItemsRecyclerView.context,
            categoryItems = categoryItems.toMutableList(),
            onItemClickListener = onClickListener,
            onItemLongClickListener = onLongClickListener
        )
        categoryItemsRecyclerView.adapter = categoryItemsAdapter
    }

    private fun deleteSelectedCategories(): Boolean {
        val selectedCategories = categoryItemsAdapter.categoryItems
            .filter { category -> category.isSelected }
            .map { category -> category.id }
        CategoriesContext.deleteCategories(selectedCategories)

        val remainingCategories = categoryItemsAdapter.categoryItems
            .filter { category -> !category.isSelected }

        categoryItemsAdapter.categoryItems.clear()
        categoryItemsAdapter.categoryItems.addAll(remainingCategories)

        resetSelection()

        return true
    }

    private fun showAddCategoryDialog(): Boolean {
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
            .first { category -> category.isSelected }

        val dialogFragment = EditCategoryDialogFragment(categoryItem)
        dialogFragment.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.Theme_LyricCast_Light_Dialog
        )

        dialogFragment.show(supportFragmentManager, EditCategoryDialogFragment.TAG)
        resetSelection()

        return true
    }

    private fun selectCategory(
        item: CategoryItem,
        holder: CategoryItemsAdapter.ViewHolder
    ) {
        if (!item.isSelected) {
            selectionCount++
        } else {
            selectionCount--
        }

        when (selectionCount) {
            0 -> {
                if (categoryItemsAdapter.showCheckBox.value!!) {
                    categoryItemsAdapter.showCheckBox.value = false
                }
                showMenuActions(showDelete = false, showEdit = false)
            }
            1 -> {
                if (!categoryItemsAdapter.showCheckBox.value!!) {
                    categoryItemsAdapter.showCheckBox.value = true
                }

                showMenuActions(showAdd = false)
            }
            2 -> {
                showMenuActions(showAdd = false, showEdit = false)
            }
        }

        item.isSelected = !item.isSelected
        holder.checkBox.isChecked = item.isSelected
    }

    private fun resetSelection() {
        categoryItemsAdapter.showCheckBox.value = false
        selectionCount = 0

        showMenuActions(showDelete = false, showEdit = false)
    }

    private fun showMenuActions(
        showAdd: Boolean = true,
        showDelete: Boolean = true,
        showEdit: Boolean = true
    ) {
        menu.findItem(R.id.menu_category_manager).isVisible = showAdd
        menu.findItem(R.id.menu_delete).isVisible = showDelete
        menu.findItem(R.id.menu_edit).isVisible = showEdit
    }

    private fun observeViewModelCategory(categoryDto: EditCategoryViewModel.CategoryDto?) {
        if (categoryDto == null) {
            return
        }

        viewModel.category.value = null

        if (categoryDto.oldCategory == null) {
            CategoriesContext.saveCategory(categoryDto.category)
        } else {
            CategoriesContext.replaceCategory(categoryDto.category, categoryDto.oldCategory)
        }

        categoryItems = CategoriesContext.getCategoryItems()

        categoryItemsAdapter.categoryItems.clear()
        categoryItemsAdapter.categoryItems.addAll(categoryItems)
        categoryItemsAdapter.notifyDataSetChanged()
    }
}