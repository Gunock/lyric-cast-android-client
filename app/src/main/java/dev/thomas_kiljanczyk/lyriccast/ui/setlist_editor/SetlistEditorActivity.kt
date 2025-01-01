/*
 * Created by Tomasz Kiljanczyk on 01/01/2025, 17:58
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 01/01/2025, 17:58
 */

package dev.thomas_kiljanczyk.lyriccast.ui.setlist_editor

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.thomas_kiljanczyk.lyriccast.databinding.ActivitySetlistEditorBinding

@AndroidEntryPoint
class SetlistEditorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val binding = ActivitySetlistEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarSetlistEditor)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

}