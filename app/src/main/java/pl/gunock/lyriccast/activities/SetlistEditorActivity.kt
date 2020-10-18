/*
 * Created by Tomasz Kiljańczyk on 10/19/20 12:26 AM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/19/20 12:17 AM
 */

package pl.gunock.lyriccast.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.utils.ResourceHelper

class SetlistEditorActivity : AppCompatActivity() {
    private val tag = "SetlistEditorActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        ResourceHelper.initialize(applicationContext)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setlist_editor)
        setSupportActionBar(findViewById(R.id.toolbar_main))

        setupListeners()
    }


    private fun setupListeners() {
        // TODO: Add necessary listeners
    }

}