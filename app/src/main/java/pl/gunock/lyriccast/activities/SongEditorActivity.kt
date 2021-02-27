/*
 * Created by Tomasz Kiljańczyk on 2/27/21 2:30 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/27/21 2:24 AM
 */

package pl.gunock.lyriccast.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
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

// TODO: Make section name case insensitive
class SongEditorActivity : AppCompatActivity() {
    private companion object {
        const val TAG = "SongEditorActivity"
    }

    inner class SectionNameTextWatcher : TextWatcher {
        var ignoreBeforeTextChanged: Boolean = false

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            if (ignoreBeforeTextChanged) {
                ignoreBeforeTextChanged = false
                return
            }

            val oldText = s.toString()
            if (tabCountMap[oldText] == 1) {
                sectionLyrics.remove(oldText)
                tabCountMap.remove(oldText)
            } else {
                tabCountMap[oldText] = tabCountMap[oldText]!! - 1
            }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val newText = s.toString()
            selectedTab.text = newText

            if (sectionLyrics.containsKey(newText)) {
                sectionLyricsInput.setText(sectionLyrics[newText]!!)
                tabCountMap[newText] = tabCountMap[newText]!! + 1
            } else {
                sectionLyrics[newText] = sectionLyricsInput.text.toString()
                tabCountMap[newText] = 1
            }
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }

    private lateinit var sectionNameInput: TextInputLayout
    private lateinit var sectionLyricsInput: EditText
    private lateinit var songSectionTabLayout: TabLayout
    private lateinit var selectedTab: TabLayout.Tab

    private val textWatcher: SectionNameTextWatcher = SectionNameTextWatcher()
    private val sectionLyrics: MutableMap<String, String> = mutableMapOf()
    private val tabCountMap: MutableMap<String, Int> = mutableMapOf()

    private var newSectionCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_song_editor)
        setSupportActionBar(findViewById(R.id.toolbar_main))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        sectionNameInput = findViewById(R.id.text_view_section_name)
        sectionLyricsInput = findViewById(R.id.text_input_section_lyrics)
        songSectionTabLayout = findViewById(R.id.tab_layout_song_section)

        val songTitle = intent.getStringExtra("songTitle")
        if (songTitle != null) {
            loadSongData(songTitle)
            selectedTab = songSectionTabLayout.getTabAt(0)!!
        } else {
            selectedTab = songSectionTabLayout.getTabAt(0)!!
            sectionLyrics[selectedTab.text.toString()] = ""
        }

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
        sectionNameInput.editText!!.addTextChangedListener(textWatcher)

        sectionLyricsInput.addTextChangedListener(
            InputTextChangeListener {
                sectionLyrics[selectedTab.text.toString()] = it
            })

        songSectionTabLayout.addOnTabSelectedListener(
            TabItemSelectedListener {
                selectedTab = it!!

                textWatcher.ignoreBeforeTextChanged = true

                when (val tabText = it.text.toString()) {
                    getString(R.string.add) -> {
                        if (songSectionTabLayout.tabCount <= 1) {
                            return@TabItemSelectedListener
                        }

                        val newSectionText = getString(R.string.new_section)
                        while (
                            tabCountMap.keys.any { it1 ->
                                it1.contains(newSectionText)
                                        && it1.split(" ").last() == newSectionCount.toString()
                            }
                        ) {
                            newSectionCount++
                        }

                        val newTabText =
                            getString(R.string.new_section_template).format(newSectionCount)
                        sectionLyricsInput.setText("")
                        sectionNameInput.editText!!.setText(newTabText)

                        val newAddTab = songSectionTabLayout.newTab()
                        newAddTab.text = getString(R.string.add)
                        addTab(newAddTab)
                    }
                    else -> {
                        sectionNameInput.editText!!.setText(tabText)
                        sectionLyricsInput.setText(sectionLyrics[tabText])
                    }
                }
            }
        )

        findViewById<Button>(R.id.button_save_song).setOnClickListener {
            saveSong()
            finish()
        }

        findViewById<ImageButton>(R.id.button_move_section_left).setOnClickListener {
            moveTabLeft(selectedTab)
        }

        findViewById<ImageButton>(R.id.button_move_section_right).setOnClickListener {
            moveTabRight(selectedTab)
        }

        findViewById<Button>(R.id.button_delete_section).setOnClickListener {
            removeTab(selectedTab)
        }
    }

    private fun saveSong() {
        val songTitleInput: TextInputLayout = findViewById(R.id.text_view_song_title)

        val song = SongMetadataModel()
        song.title = songTitleInput.editText!!.editableText.toString()
        val songNormalizedTitle = song.title.normalize()

        val addText = getString(R.string.add)
        song.lyricsFilename = "$songNormalizedTitle.json"
        song.presentation = sectionLyrics.keys.toList()
            .filter { it != addText }

        val songLyrics = SongLyricsModel()
        songLyrics.lyrics = sectionLyrics.filter { it.key != addText }

        val songFilePath = "${SongsContext.songsDirectory}$songNormalizedTitle"

        Log.d(TAG, "Saving song")
        Log.d(TAG, song.toJSON().toString())
        File("$songFilePath.metadata.json").create()
            .writeText(song.toJSON())

        Log.d(TAG, "Saving lyrics")
        Log.d(TAG, songLyrics.toJSON().toString())
        File("$songFilePath.json").create()
            .writeText(songLyrics.toJSON())

        SongsContext.addSong(song)
    }

    private fun loadSongData(songTitle: String) {
        val songTitleInput: TextInputLayout = findViewById(R.id.text_view_song_title)
        songTitleInput.editText!!.setText(songTitle)

        val songMetadata = SongsContext.getSongMetadata(songTitle)
        val songLyrics = SongsContext.getSongLyrics(songTitle).lyrics

        songSectionTabLayout.removeAllTabs()

        for (sectionName in songMetadata.presentation) {
            val newTab = songSectionTabLayout.newTab()
            addTab(newTab, sectionName)

            sectionNameInput.editText!!.setText(sectionName)

            sectionLyrics[sectionName] = songLyrics[sectionName]!!

            newTab.text = sectionName
        }

        val newAddTab = songSectionTabLayout.newTab()
        addTab(newAddTab)
        newAddTab.text = getString(R.string.add)

        val sectionLyricsInput = findViewById<EditText>(R.id.text_input_section_lyrics)
        sectionLyricsInput.setText(songLyrics[songMetadata.presentation.first()]!!)
    }

    private fun moveTabLeft(tab: TabLayout.Tab) {
        val position = tab.position
        if (position == 0) {
            return
        }

        val otherTab = songSectionTabLayout.getTabAt(position - 1)!!

        val newTab = songSectionTabLayout.newTab()
        newTab.text = tab.text

        val newOtherTab = songSectionTabLayout.newTab()
        newOtherTab.text = otherTab.text

        songSectionTabLayout.removeTab(tab)
        songSectionTabLayout.removeTab(otherTab)

        songSectionTabLayout.addTab(newTab, position - 1)
        songSectionTabLayout.addTab(newOtherTab, position)

        songSectionTabLayout.selectTab(newTab)
    }

    private fun moveTabRight(tab: TabLayout.Tab) {
        val position = tab.position
        if (position == songSectionTabLayout.tabCount - 2) {
            return
        }

        val otherTab = songSectionTabLayout.getTabAt(position + 1)!!

        val newTab = songSectionTabLayout.newTab()
        newTab.text = tab.text

        val newOtherTab = songSectionTabLayout.newTab()
        newOtherTab.text = otherTab.text

        songSectionTabLayout.removeTab(tab)
        songSectionTabLayout.removeTab(otherTab)

        songSectionTabLayout.addTab(newOtherTab, position)
        songSectionTabLayout.addTab(newTab, position + 1)

        songSectionTabLayout.selectTab(newTab)
    }

    private fun addTab(tab: TabLayout.Tab, tabText: String = "") {
        songSectionTabLayout.addTab(tab)

        if (tabText.isBlank()) {
            return
        }

        if (tabCountMap.containsKey(tabText)) {
            tabCountMap[tabText] = tabCountMap[tabText]!! + 1
        } else {
            tabCountMap[tabText] = 1
        }
    }

    private fun removeTab(tab: TabLayout.Tab) {
        val tabText = tab.text.toString()
        songSectionTabLayout.removeTab(tab)

        val tabCount = tabCountMap[tabText]!! - 1

        if (tabCount == 0) {
            tabCountMap.remove(tabText)
            sectionLyrics.remove(tabText)
        } else {
            tabCountMap[tabText] = tabCount
        }
    }
}