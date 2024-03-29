/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 21:06
 */

package pl.gunock.lyriccast.ui.setlist_editor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.ActivitySetlistEditorBinding
import pl.gunock.lyriccast.shared.extensions.loadAd

@AndroidEntryPoint
class SetlistEditorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = getColor(R.color.background_1)

        val binding = ActivitySetlistEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarSetlistEditor)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding.contentSetlistEditor.advSetlistEditor.loadAd()
    }

}