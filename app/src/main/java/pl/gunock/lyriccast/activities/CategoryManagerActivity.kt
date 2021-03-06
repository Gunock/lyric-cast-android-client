/*
 * Created by Tomasz Kiljańczyk on 3/6/21 11:16 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/6/21 11:12 PM
 */

package pl.gunock.lyriccast.activities

import android.os.Bundle
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.adapters.CategoryItemsAdapter
import pl.gunock.lyriccast.adapters.ColorSpinnerAdapter
import pl.gunock.lyriccast.models.CategoryItem
import pl.gunock.lyriccast.models.ColorItem

class CategoryManagerActivity : AppCompatActivity() {

    private lateinit var colorSpinner: Spinner
    private lateinit var categoryItemsRecyclerView: RecyclerView

    private var categoryItems: Set<CategoryItem> = setOf()
    private lateinit var categoryItemsAdapter: CategoryItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_category_manager)
        setSupportActionBar(findViewById(R.id.toolbar_main))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        colorSpinner = findViewById(R.id.spn_category_color)
        categoryItemsRecyclerView = findViewById(R.id.rcv_categories)

        with(categoryItemsRecyclerView) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(baseContext)
        }

        setupCategories()
        setupColorSpinner()

        setupListeners()
    }

    private fun setupListeners() {
        // TODO: Add necessary listeners
    }

    private fun setupColorSpinner() {
        val colorNames = resources.getStringArray(R.array.category_color_names)
        val colorValues = resources.getIntArray(R.array.category_color_values)
        val colors = Array(colorNames.size) { position ->
            ColorItem(colorNames[position], colorValues[position])
        }

        val colorSpinnerAdapter = ColorSpinnerAdapter(
            baseContext,
            colors
        )
        colorSpinner.adapter = colorSpinnerAdapter
    }

    private fun setupCategories() {
        val colorValues = resources.getIntArray(R.array.category_color_values)

        categoryItems = setOf(
            CategoryItem("Test1", colorValues[1]),
            CategoryItem("Test2", colorValues[1]),
            CategoryItem("Test3", colorValues[0]),
            CategoryItem("Test4", colorValues[2])
        )

        categoryItemsAdapter = CategoryItemsAdapter(categoryItems.toMutableList())
        categoryItemsRecyclerView.adapter = categoryItemsAdapter
    }

}