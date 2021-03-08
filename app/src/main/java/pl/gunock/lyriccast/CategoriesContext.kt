/*
 * Created by Tomasz Kiljańczyk on 3/8/21 11:19 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/8/21 11:17 PM
 */

package pl.gunock.lyriccast

import pl.gunock.lyriccast.models.Category
import pl.gunock.lyriccast.models.CategoryItem
import java.util.*

object CategoriesContext {

    private var categories: SortedSet<Category> = sortedSetOf()
    private var categoryMap: MutableMap<Long, Category> = hashMapOf()

    fun containsCategory(name: String): Boolean {
        return categories.contains(Category(name))
    }

    fun getCategoryItems(): Set<CategoryItem> {
        return categories.map { category -> CategoryItem(category) }.toSortedSet()
    }

    fun getCategories(): Set<Category> {
        return categories.toSet()
    }

    fun addCategory(category: Category) {
        val id = System.currentTimeMillis()
        val categoryWithId = Category(id, category)

        categories.add(categoryWithId)
        categoryMap[id] = categoryWithId
    }

    fun getCategory(id: Long?): Category? {
        if (id == null) {
            return null
        }

        return categoryMap[id]
    }

    fun replaceCategory(newCategory: Category, oldCategory: Category) {
        categories =
            categories.filter { category -> category.name == oldCategory.name }.toSortedSet()

        categories.add(newCategory)
        categoryMap[newCategory.id] = newCategory
    }

}