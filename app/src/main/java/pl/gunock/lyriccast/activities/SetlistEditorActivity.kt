/*
 * Created by Tomasz Kiljańczyk on 10/25/20 10:05 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/23/20 10:04 PM
 */

package pl.gunock.lyriccast.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import pl.gunock.lyriccast.R

class SetlistEditorActivity : AppCompatActivity() {
    private val tag = "SetlistEditorActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_setlist_editor)
        setSupportActionBar(findViewById(R.id.toolbar_main))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        setupListeners()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true // Ignores default behaviour
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun setupListeners() {
        // TODO: Add necessary listeners
    }

}