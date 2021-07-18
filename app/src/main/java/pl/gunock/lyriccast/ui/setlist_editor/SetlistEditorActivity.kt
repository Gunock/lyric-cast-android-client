/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 17/07/2021, 11:25
 */

package pl.gunock.lyriccast.ui.setlist_editor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pl.gunock.lyriccast.databinding.ActivitySetlistEditorBinding
import pl.gunock.lyriccast.extensions.loadAd

class SetlistEditorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySetlistEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarSetlistEditor)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding.contentSetlistEditor.advSetlistEditor.loadAd()
    }

}