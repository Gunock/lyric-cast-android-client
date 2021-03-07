/*
 * Created by Tomasz Kiljańczyk on 3/7/21 11:44 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/7/21 10:57 PM
 */

package pl.gunock.lyriccast.models

class CategoryItem(
    category: Category,
    var isSelected: Boolean = false
) : Category(category.name, category.color) {

    constructor(name: String, color: String) : this(Category(name, color))

}
