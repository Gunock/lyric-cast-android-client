/*
 * Created by Tomasz Kiljańczyk on 3/28/21 3:19 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/27/21 11:11 PM
 */

package pl.gunock.lyriccast.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.runBlocking
import pl.gunock.lyriccast.LyricCastApplication
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.adapters.CategoryItemsAdapter
import pl.gunock.lyriccast.datamodel.LyricCastRepository
import pl.gunock.lyriccast.datamodel.entities.Category
import pl.gunock.lyriccast.fragments.dialog.EditCategoryDialogFragment
import pl.gunock.lyriccast.misc.EditCategoryViewModel
import pl.gunock.lyriccast.misc.SelectionTracker
import pl.gunock.lyriccast.models.CategoryItem

class CategoryManagerActivity : AppCompatActivity() {

    private lateinit var repository: LyricCastRepository

    private lateinit var menu: Menu
    private lateinit var categoryItemsRecyclerView: RecyclerView

    private lateinit var viewModel: EditCategoryViewModel

    private var categoryItems: Set<CategoryItem> = setOf()
    private lateinit var categoryItemsAdapter: CategoryItemsAdapter
    private lateinit var selectionTracker: SelectionTracker<CategoryItemsAdapter.ViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_category_manager)
        setSupportActionBar(findViewById(R.id.toolbar_category_manager))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        repository = (application as LyricCastApplication).repository

        // TODO: Possible leak
        viewModel = ViewModelProvider(this).get(EditCategoryViewModel::class.java)
        viewModel.categoryNames.value = runBlocking { repository.getCategories() }
            .map { category -> category.name }
            .toSet()

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
        runBlocking {
            categoryItems = repository.getCategories()
                .map { category -> CategoryItem(category) }
                .toSet()
        }

        selectionTracker = SelectionTracker(categoryItemsRecyclerView, this::onCategoryClick)

        categoryItemsAdapter = CategoryItemsAdapter(
            categoryItemsRecyclerView.context,
            categoryItems = categoryItems.toMutableList(),
            selectionTracker = selectionTracker
        )
        categoryItemsRecyclerView.adapter = categoryItemsAdapter
    }

    private fun onCategoryClick(
        holder: CategoryItemsAdapter.ViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        val item = categoryItemsAdapter.categoryItems[position]
        if (isLongClick || selectionTracker.count != 0) {
            selectCategory(item, holder)
            return true
        }
        return false
    }

    private fun deleteSelectedCategories(): Boolean {
        val selectedCategories = categoryItemsAdapter.categoryItems
            .filter { item -> item.isSelected }
            .map { items -> items.category.id }

        runBlocking {
            repository.deleteCategories(selectedCategories)
        }

        val remainingCategories = categoryItemsAdapter.categoryItems
            .filter { category -> !category.isSelected }

        categoryItemsAdapter.categoryItems.clear()
        categoryItemsAdapter.categoryItems.addAll(remainingCategories)

        resetSelection()

        return true
    }

    private fun showAddCategoryDialog(): Boolean {
        viewModel.category.observe(this, this::observeViewModelCategory)
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

        viewModel.category.observe(this, this::observeViewModelCategory)

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
        when (selectionTracker.countAfter) {
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
        categoryItemsAdapter.categoryItems.forEach { categoryItem ->
            categoryItem.isSelected = false
        }
        selectionTracker.reset()

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

    private fun observeViewModelCategory(category: Category?) {
        if (category == null) {
            return
        }

        runBlocking {
            repository.upsertCategory(viewModel.category.value!!)
        }

        runBlocking {
            categoryItems = repository.getCategories()
                .map { category -> CategoryItem(category) }
                .toSet()
        }
        categoryItemsAdapter.categoryItems.clear()
        categoryItemsAdapter.categoryItems.addAll(categoryItems)
        categoryItemsAdapter.notifyDataSetChanged()

        viewModel.category.removeObservers(this)
        viewModel.category.value = null
    }
}