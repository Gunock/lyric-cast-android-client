/*
 * Created by Tomasz Kiljanczyk on 14/05/2021, 00:06
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 14/05/2021, 00:06
 */

package pl.gunock.lyriccast.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import pl.gunock.lyriccast.databinding.ActivitySetlistEditorBinding

class SetlistEditorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySetlistEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarSetlistEditor)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val adRequest = AdRequest.Builder().build()
        binding.contentSetlistEditor.advSetlistEditor.loadAd(adRequest)
    }

}