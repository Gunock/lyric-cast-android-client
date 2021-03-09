/*
 * Created by Tomasz Kiljańczyk on 3/9/21 2:21 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/9/21 2:19 AM
 */

package pl.gunock.lyriccast

import android.util.Log
import org.json.JSONObject
import pl.gunock.lyriccast.models.Category
import pl.gunock.lyriccast.models.CategoryItem
import java.io.File
import java.io.FilenameFilter
import java.util.*

object CategoriesContext {
    private const val TAG = "CategoriesContext"

    private var categories: SortedSet<Category> = sortedSetOf()
    private var categoryMap: MutableMap<Long, Category> = hashMapOf()

    var categoriesDirectory: String = ""

    fun containsCategory(name: String): Boolean {
        return categories.contains(Category(name))
    }

    fun getCategoryItems(): Set<CategoryItem> {
        return categories.map { category -> CategoryItem(category) }.toSortedSet()
    }

    fun getCategories(): Set<Category> {
        return categories.toSet()
    }

    fun getCategory(id: Long?): Category? {
        id ?: return null
        return if (id == Long.MIN_VALUE) return null else categoryMap[id]!!
    }

    fun replaceCategory(newCategory: Category, oldCategory: Category) {
        val json = newCategory.toJson()
        val categoryFile = File("${categoriesDirectory}/${newCategory.id}.json")
        File(categoriesDirectory).mkdirs()
        categoryFile.writeText(json.toString())

        categories.remove(oldCategory)
        categories.add(newCategory)
        categoryMap[newCategory.id] = newCategory
    }

    fun saveCategory(category: Category) {
        val id = System.currentTimeMillis()
        val categoryWithId = Category(id, category)

        val json = categoryWithId.toJson()
        val categoryFile = File("${categoriesDirectory}/${id}.json")
        File(categoriesDirectory).mkdirs()
        categoryFile.writeText(json.toString())

        categories.add(categoryWithId)
        categoryMap[id] = categoryWithId
    }

    fun deleteCategories(ids: Collection<Long>) {
        for (id in ids) {
            val categoryFile = File("${categoriesDirectory}/${id}.json")
            categoryFile.delete()

            categories.remove(categoryMap[id])
            categoryMap.remove(id)
        }

        SongsContext.removeCategories(ids)
    }

    fun loadCategories() {
        val loadedCategories: SortedSet<Category> = sortedSetOf()
        val fileFilter = FilenameFilter { _, name -> name.endsWith(".json") }
        val fileList = File(categoriesDirectory).listFiles(fileFilter)

        if (fileList == null || fileList.isEmpty()) {
            return
        }

        for (file in fileList) {
            Log.d(TAG, "Reading file: ${file.name}")
            val json = JSONObject(file.readText(Charsets.UTF_8))
            val category = Category(json)
            loadedCategories.add(category)
        }
        Log.d(TAG, "Parsed category files: $loadedCategories")

        categories = loadedCategories
        categoryMap = loadedCategories.map { category -> category.id to category }
            .toMap()
            .toMutableMap()
    }
}