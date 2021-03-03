/*
 * Created by Tomasz Kiljańczyk on 2/27/21 4:17 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/27/21 11:59 AM
 */

package pl.gunock.lyriccast.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pl.gunock.lyriccast.R

class SetlistEditorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_setlist_editor)
        setSupportActionBar(findViewById(R.id.toolbar_main))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        setupListeners()
    }

    private fun setupListeners() {
        // TODO: Add necessary listeners
    }

}