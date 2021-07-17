/*
 * Created by Tomasz Kiljanczyk on 17/07/2021, 11:19
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 17/07/2021, 10:43
 */

package pl.gunock.lyriccast.activities

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