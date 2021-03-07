/*
 * Created by Tomasz Kiljańczyk on 3/8/21 12:43 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/8/21 12:39 AM
 */

package pl.gunock.lyriccast.activities

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.AllCaps
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputLayout
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.enums.NameValidationState
import pl.gunock.lyriccast.extensions.moveTabLeft
import pl.gunock.lyriccast.extensions.moveTabRight
import pl.gunock.lyriccast.listeners.InputTextChangedListener
import pl.gunock.lyriccast.listeners.ItemSelectedTabListener
import pl.gunock.lyriccast.models.SongLyrics
import pl.gunock.lyriccast.models.SongMetadata

class SongEditorActivity : AppCompatActivity() {
    private var intentSongTitle: String? = null
    private var songTitles: Set<String> = setOf()

    private lateinit var sectionNameInput: EditText
    private lateinit var songTitleInputLayout: TextInputLayout
    private lateinit var songTitleInput: EditText
    private lateinit var sectionLyricsInput: EditText
    private lateinit var songSectionTabLayout: TabLayout
    private lateinit var selectedTab: TabLayout.Tab
    private lateinit var categorySpinner: Spinner

    private val sectionNameTextWatcher: SectionNameTextWatcher = SectionNameTextWatcher()
    private val songTitleTextWatcher: SongTitleTextWatcher = SongTitleTextWatcher()
    private val sectionLyrics: MutableMap<String, String> = mutableMapOf()
    private val tabCountMap: MutableMap<String, Int> = mutableMapOf()

    private var newSectionCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_song_editor)
        setSupportActionBar(findViewById(R.id.toolbar_main))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        songTitleInputLayout = findViewById(R.id.tv_song_title)
        songTitleInput = findViewById(R.id.tin_song_title)
        sectionNameInput = findViewById(R.id.tin_section_name)
        sectionLyricsInput = findViewById(R.id.tin_section_lyrics)
        songSectionTabLayout = findViewById(R.id.tbl_song_section)
        categorySpinner = findViewById(R.id.spn_song_editor_category)

        sectionNameInput.filters = arrayOf<InputFilter>(AllCaps())

        songTitles = SongsContext.getSongMap().keys

        setupCategorySpinner()

        intentSongTitle = intent.getStringExtra("songTitle")
        if (intentSongTitle != null) {
            loadSongData(intentSongTitle!!)
            selectedTab = songSectionTabLayout.getTabAt(0)!!

            val songMetadata = SongsContext.getSongMetadata(intentSongTitle!!)!!
            categorySpinner.setSelection(SongsContext.categories.indexOf(songMetadata.category))
        } else {
            selectedTab = songSectionTabLayout.getTabAt(0)!!
            sectionLyrics[selectedTab.text.toString()] = ""
            tabCountMap[selectedTab.text.toString()] = 1
        }

        setupListeners()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_song_editor, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_save -> {
                if (saveSong()) {
                    finish()
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupCategorySpinner() {
        val categorySpinnerAdapter = ArrayAdapter(
            baseContext,
            android.R.layout.simple_spinner_item,
            SongsContext.categories.toList()
        )
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categorySpinnerAdapter
    }

    private fun setupListeners() {
        songTitleInput.addTextChangedListener(songTitleTextWatcher)

        sectionNameInput.addTextChangedListener(sectionNameTextWatcher)

        sectionLyricsInput.addTextChangedListener(
            InputTextChangedListener { newText ->
                sectionLyrics[selectedTab.text.toString()] = newText
            })

        songSectionTabLayout.addOnTabSelectedListener(
            ItemSelectedTabListener { tab ->
                selectedTab = tab!!

                sectionNameTextWatcher.ignoreBeforeTextChanged = true

                when (val tabText = tab.text.toString()) {
                    getString(R.string.button_add) -> {
                        if (songSectionTabLayout.tabCount <= 1) {
                            return@ItemSelectedTabListener
                        }

                        sectionLyricsInput.setText("")

                        val newSectionText = getString(R.string.input_new_section)
                        while (
                            tabCountMap.keys.any { sectionName ->
                                sectionName.contains(newSectionText)
                                        && sectionName.split(" ")
                                    .last() == newSectionCount.toString()
                            }
                        ) {
                            newSectionCount++
                        }

                        val newTabText =
                            getString(R.string.input_new_section_template).format(newSectionCount)
                        sectionNameInput.setText(newTabText)
                        newSectionCount++

                        val newAddTab = songSectionTabLayout.newTab()
                        newAddTab.text = getString(R.string.button_add)
                        addTab(newAddTab)
                    }
                    else -> {
                        sectionNameTextWatcher.ignoreOnTextChanged = true
                        sectionNameInput.setText(tabText)
                        sectionLyricsInput.setText(sectionLyrics[tabText])
                    }
                }
            }
        )

        findViewById<ImageButton>(R.id.btn_move_section_left).setOnClickListener {
            songSectionTabLayout.moveTabLeft(selectedTab)
        }

        findViewById<ImageButton>(R.id.btn_move_section_right).setOnClickListener {
            songSectionTabLayout.moveTabRight(selectedTab)
        }

        findViewById<Button>(R.id.btn_delete_section).setOnClickListener {
            removeTab(selectedTab)
        }
    }

    private fun validateSongTitle(songTitle: String): NameValidationState {
        return if (songTitle.isBlank()) {
            NameValidationState.EMPTY
        } else if (intentSongTitle != songTitle && songTitles.contains(songTitle)) {
            NameValidationState.ALREADY_IN_USE
        } else {
            NameValidationState.VALID
        }
    }

    private fun saveSong(): Boolean {
        val songTitle = songTitleInput.text.toString()

        if (validateSongTitle(songTitle) != NameValidationState.VALID) {
            songTitleInput.setText(songTitle)
            songTitleInput.requestFocus()
            return false
        }

        val addText = getString(R.string.button_add)

        val presentation: MutableList<String> = mutableListOf()
        for (position in 0 until songSectionTabLayout.tabCount) {
            val tab = songSectionTabLayout.getTabAt(position)!!

            if (tab.text == addText) {
                continue
            }

            presentation.add(tab.text.toString())
        }

        val song = SongMetadata()
        song.title = songTitle
        song.category = categorySpinner.selectedItem?.toString()
        song.presentation = presentation

        val songLyrics = SongLyrics()
        songLyrics.lyrics = sectionLyrics.filter { lyricsMapEntry -> lyricsMapEntry.key != addText }

        if (intentSongTitle != null) {
            SongsContext.replaceSong(intentSongTitle!!, song, songLyrics)
        } else {
            SongsContext.addSong(song, songLyrics)
        }
        return true
    }

    private fun loadSongData(songTitle: String) {
        val songTitleInput: TextInputLayout = findViewById(R.id.tv_song_title)
        songTitleInput.editText!!.setText(songTitle)

        val songMetadata = SongsContext.getSongMetadata(songTitle)!!
        val songLyrics = SongsContext.getSongLyrics(songTitle)!!.lyrics

        songSectionTabLayout.removeAllTabs()

        for (sectionName in songMetadata.presentation) {
            val newTab = songSectionTabLayout.newTab()
            addTab(newTab, sectionName)

            sectionLyricsInput.setText(sectionName)

            sectionLyrics[sectionName] = songLyrics[sectionName]!!

            newTab.text = sectionName
        }

        val newAddTab = songSectionTabLayout.newTab()
        addTab(newAddTab)
        newAddTab.text = getString(R.string.button_add)

        val sectionLyricsInput = findViewById<EditText>(R.id.tin_section_lyrics)
        sectionLyricsInput.setText(songLyrics[songMetadata.presentation.first()]!!)
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

        if (tabText == getString(R.string.button_add)) {
            return
        }

        val tabCount = tabCountMap[tabText]!! - 1

        if (tabCount <= 0) {
            tabCountMap.remove(tabText)
            sectionLyrics.remove(tabText)
        } else {
            tabCountMap[tabText] = tabCount
        }

        if (songSectionTabLayout.tabCount > 2) {
            songSectionTabLayout.removeTab(tab)
        } else {
            newSectionCount = 1
            val newTabText = getString(R.string.input_new_section_template).format(newSectionCount)

            tabCountMap[tabText] = 1
            tab.text = newTabText
        }
    }

    inner class SongTitleTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            val newText = s.toString()

            when (validateSongTitle(newText)) {
                NameValidationState.EMPTY -> {
                    songTitleInputLayout.error = " "
                    songTitleInput.error = "Please enter song title"
                }
                NameValidationState.ALREADY_IN_USE -> {
                    songTitleInputLayout.error = " "
                    songTitleInput.error = "Song title already in use"
                }
                NameValidationState.VALID -> {
                    songTitleInputLayout.error = null
                    songTitleInput.error = null
                }
            }
        }
    }

    inner class SectionNameTextWatcher : TextWatcher {
        var ignoreBeforeTextChanged: Boolean = false
        var ignoreOnTextChanged: Boolean = false

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            if (ignoreBeforeTextChanged) {
                ignoreBeforeTextChanged = false
                return
            }

            val oldText = s.toString()
            val oldTabCount = tabCountMap[oldText]!!
            if (oldTabCount <= 1) {
                sectionLyrics.remove(oldText)
                tabCountMap.remove(oldText)
            } else {
                tabCountMap[oldText] = oldTabCount - 1
            }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (ignoreOnTextChanged) {
                ignoreOnTextChanged = false
                return
            }

            val newText = s.toString()
            selectedTab.text = newText

            tabCountMap[newText] =
                if (tabCountMap.containsKey(newText)) tabCountMap[newText]!! + 1 else 1

            if (sectionLyrics.containsKey(newText)) {
                sectionLyricsInput.setText(sectionLyrics[newText]!!)
            } else {
                sectionLyrics[newText] = sectionLyricsInput.text.toString()
            }
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }
}