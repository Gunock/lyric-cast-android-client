/*
 * Created by Tomasz Kiljańczyk on 2/25/21 10:00 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/25/21 9:57 PM
 */

package pl.gunock.lyriccast.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import pl.gunock.lyriccast.R

class SetlistEditorActivity : AppCompatActivity() {
    private companion object {
        private const val TAG = "SetlistEditorActivity"
    }

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