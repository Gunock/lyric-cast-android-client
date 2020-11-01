/*
 * Created by Tomasz Kiljańczyk on 11/1/20 3:44 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 11/1/20 3:43 PM
 */

package pl.gunock.lyriccast.activities

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputLayout
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.extensions.create
import pl.gunock.lyriccast.extensions.normalize
import pl.gunock.lyriccast.extensions.writeText
import pl.gunock.lyriccast.listeners.InputTextChangeListener
import pl.gunock.lyriccast.listeners.TabItemSelectedListener
import pl.gunock.lyriccast.models.SongLyricsModel
import pl.gunock.lyriccast.models.SongMetadataModel
import java.io.File

class SongEditorActivity : AppCompatActivity() {
    private val tag = "SongEditorActivity"

    private var sectionNameInput: TextInputLayout? = null
    private var selectedTab: TabLayout.Tab? = null

    private val sectionLyrics: MutableMap<TabLayout.Tab, String> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_song_editor)
        setSupportActionBar(findViewById(R.id.toolbar_main))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        sectionNameInput = findViewById(R.id.text_view_section_name)
        val songSectionTabLayout: TabLayout = findViewById(R.id.tab_layout_song_section)
        selectedTab = songSectionTabLayout.getTabAt(songSectionTabLayout.selectedTabPosition)
        sectionLyrics[selectedTab!!] = ""

        setupCategorySpinner()

        setupListeners()
    }

    private fun setupCategorySpinner() {
        val categorySpinnerAdapter = ArrayAdapter(
            baseContext,
            android.R.layout.simple_spinner_item,
            SongsContext.categories.toList()
        )
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        findViewById<Spinner>(R.id.spinner_song_editor_category).apply {
            adapter = categorySpinnerAdapter
        }
    }

    private fun setupListeners() {
        sectionNameInput!!.editText!!.addTextChangedListener(InputTextChangeListener {
            selectedTab!!.text = it
        })

        findViewById<EditText>(R.id.text_input_section_lyrics).addTextChangedListener(
            InputTextChangeListener {
                sectionLyrics[selectedTab!!] = it
            })

        findViewById<Button>(R.id.button_save_song).setOnClickListener {
            saveSong()
            finish()
        }

        findViewById<TabLayout>(R.id.tab_layout_song_section).addOnTabSelectedListener(
            TabItemSelectedListener {
                selectedTab = it

                if (it!!.text == getString(R.string.add)) {
                    sectionNameInput!!.editText!!.setText(getString(R.string.new_section))

                    sectionLyrics[it] = ""
                    findViewById<EditText>(R.id.text_input_section_lyrics).setText("")

                    val newAddTab = it.parent!!.newTab()
                    newAddTab.text = getString(R.string.add)
                    it.parent!!.addTab(newAddTab)
                } else {
                    sectionNameInput!!.editText!!.setText(it.text)
                    findViewById<EditText>(R.id.text_input_section_lyrics).setText(sectionLyrics[it])
                }
            })
    }

    private fun saveSong() {
        val songTitleInput: TextInputLayout = findViewById(R.id.text_view_song_title)

        val song = SongMetadataModel()
        song.title = songTitleInput.editText!!.editableText.toString()
        val songNormalizedTitle = song.title.normalize()

        song.lyricsFilename = "$songNormalizedTitle.json"
        song.presentation = List(sectionLyrics.size) {
            sectionLyrics.keys.toList()[it].text.toString()
        }

        val songLyrics = SongLyricsModel()
        songLyrics.lyrics = sectionLyrics.map { it.key.text.toString() to it.value }.toMap()

        val songFilePath = "${SongsContext.songsDirectory}$songNormalizedTitle"

        Log.d(tag, "Saving song")
        Log.d(tag, song.toJSON().toString())
        File("$songFilePath.metadata.json").create()
            .writeText(song.toJSON())

        Log.d(tag, "Saving lyrics")
        Log.d(tag, songLyrics.toJSON().toString())
        File("$songFilePath.json").create()
            .writeText(songLyrics.toJSON())

        SongsContext.addSong(song)
    }

}