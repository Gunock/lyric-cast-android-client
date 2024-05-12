/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 21:06
 */

package pl.gunock.lyriccast.ui.setlist_editor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.application.AppSettings
import pl.gunock.lyriccast.databinding.ActivitySetlistEditorBinding
import pl.gunock.lyriccast.shared.extensions.loadAd
import javax.inject.Inject

@AndroidEntryPoint
class SetlistEditorActivity : AppCompatActivity() {

    @Inject
    lateinit var dataStore: DataStore<AppSettings>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = getColor(R.color.background_1)

        val binding = ActivitySetlistEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarSetlistEditor)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        CoroutineScope(Dispatchers.Main).launch {
            binding.contentSetlistEditor.advSetlistEditor.loadAd(
                dataStore
            )
        }
    }

}