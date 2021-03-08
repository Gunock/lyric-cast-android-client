/*
 * Created by Tomasz Kiljańczyk on 3/7/21 11:44 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/7/21 11:16 PM
 */

package pl.gunock.lyriccast

import pl.gunock.lyriccast.models.Category
import pl.gunock.lyriccast.models.CategoryItem
import java.util.*

object CategoriesContext {

    private var categories: SortedSet<Category> = sortedSetOf()

    fun containsCategory(name: String): Boolean {
        return categories.contains(Category(name))
    }

    fun getCategoryItems(): Set<CategoryItem> {
        return categories.map { category -> CategoryItem(category) }.toSortedSet()
    }

    fun addCategory(category: Category) {
        categories.add(category)
    }

    fun replaceCategory(newCategory: Category, oldCategory: Category) {
        categories =
            categories.filter { category -> category.name == oldCategory.name }.toSortedSet()

        categories.add(newCategory)
    }

}