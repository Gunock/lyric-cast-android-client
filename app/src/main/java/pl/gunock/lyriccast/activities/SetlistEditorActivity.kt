/*
 * Created by Tomasz Kiljańczyk on 3/13/21 3:21 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/12/21 11:06 PM
 */

package pl.gunock.lyriccast.activities

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import pl.gunock.lyriccast.R

class SetlistEditorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_setlist_editor)
        setSupportActionBar(findViewById(R.id.toolbar_setlist_editor))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_setlist_editor, menu)

        val deleteActionItem = menu.findItem(R.id.menu_delete)
        deleteActionItem.isVisible = false

        val duplicateActionItem = menu.findItem(R.id.menu_duplicate)
        duplicateActionItem.isVisible = false

        return true
    }

}