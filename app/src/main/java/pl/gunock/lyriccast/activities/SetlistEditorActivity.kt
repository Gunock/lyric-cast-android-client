/*
 * Created by Tomasz Kiljanczyk on 4/5/21 4:34 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 4:34 PM
 */

package pl.gunock.lyriccast.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pl.gunock.lyriccast.R

class SetlistEditorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_setlist_editor)
        setSupportActionBar(findViewById(R.id.toolbar_setlist_editor))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

}