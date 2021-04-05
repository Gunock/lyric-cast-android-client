/*
 * Created by Tomasz Kiljanczyk on 4/5/21 1:21 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 1:21 PM
 */

package pl.gunock.lyriccast.activities

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import pl.gunock.lyriccast.LyricCastApplication
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.adapters.spinner.CategorySpinnerAdapter
import pl.gunock.lyriccast.common.extensions.moveTabLeft
import pl.gunock.lyriccast.common.extensions.moveTabRight
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.datamodel.DatabaseViewModelFactory
import pl.gunock.lyriccast.datamodel.LyricCastRepository
import pl.gunock.lyriccast.datamodel.entities.Category
import pl.gunock.lyriccast.datamodel.entities.LyricsSection
import pl.gunock.lyriccast.datamodel.entities.Song
import pl.gunock.lyriccast.datamodel.entities.relations.SongWithLyricsSections
import pl.gunock.lyriccast.enums.NameValidationState
import pl.gunock.lyriccast.listeners.InputTextChangedListener
import pl.gunock.lyriccast.listeners.ItemSelectedTabListener

class SongEditorActivity : AppCompatActivity() {
    private companion object {
        const val TAG = "SongEditorActivity"
    }

    private var intentSong: Song? = null
    private lateinit var repository: LyricCastRepository
    private val databaseViewModel: DatabaseViewModel by viewModels {
        DatabaseViewModelFactory(baseContext, (application as LyricCastApplication).repository)
    }

    private lateinit var sectionNameInput: EditText
    private lateinit var songTitleInputLayout: TextInputLayout
    private lateinit var songTitleInput: EditText
    private lateinit var sectionLyricsInput: EditText
    private lateinit var songSectionTabLayout: TabLayout
    private lateinit var selectedTab: TabLayout.Tab
    private lateinit var categorySpinner: Spinner

    private val sectionNameTextWatcher: SectionNameTextWatcher = SectionNameTextWatcher()
    private val songTitleTextWatcher: SongTitleTextWatcher = SongTitleTextWatcher()

    private lateinit var songTitles: Set<String>
    private val sectionLyrics: MutableMap<String, String> = mutableMapOf()
    private val tabCountMap: MutableMap<String, Int> = mutableMapOf()

    private var newSectionCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_song_editor)
        setSupportActionBar(findViewById(R.id.toolbar_main))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        repository = (application as LyricCastApplication).repository

        songTitleInputLayout = findViewById(R.id.tv_song_title)
        songTitleInput = findViewById(R.id.tin_song_title)
        sectionNameInput = findViewById(R.id.tin_section_name)
        sectionLyricsInput = findViewById(R.id.tin_section_lyrics)
        songSectionTabLayout = findViewById(R.id.tbl_song_section)
        categorySpinner = findViewById(R.id.spn_song_editor_category)

        songTitleInput.filters = arrayOf(InputFilter.LengthFilter(30))
        sectionNameInput.filters = arrayOf(InputFilter.AllCaps(), InputFilter.LengthFilter(30))

        databaseViewModel.allSongs.observe(this) { songs ->
            songTitles = songs.map { songAndCategory -> songAndCategory.song.title }.toSet()
        }
        setupCategorySpinner()

        intentSong = intent.getParcelableExtra("song")
        Log.v(TAG, "Received song : $intentSong")
        if (intentSong != null) {
            loadSongData(intentSong!!)

            selectedTab = songSectionTabLayout.getTabAt(0)!!
        } else {
            songTitleInput.setText("")
            selectedTab = songSectionTabLayout.getTabAt(0)!!
            val sectionName = selectedTab.text.toString().trim()
            sectionLyrics[sectionName] = ""
            tabCountMap[sectionName] = 1
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
                CoroutineScope(Dispatchers.IO).launch {
                    if (saveSong()) {
                        finish()
                    }
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupCategorySpinner() {
        val categorySpinnerAdapter = CategorySpinnerAdapter(baseContext)
        categorySpinner.adapter = categorySpinnerAdapter

        databaseViewModel.allCategories.observe(this) { categories ->
            categorySpinnerAdapter.submitCollection(categories, Category.NONE)

            if (intentSong != null) {
                val categoryIndex = (categorySpinner.adapter as CategorySpinnerAdapter).categories
                    .map { category -> category.categoryId }
                    .indexOf(intentSong!!.categoryId)

                categorySpinner.setSelection(categoryIndex)
            }
        }
    }

    private fun setupListeners() {
        songTitleInput.addTextChangedListener(songTitleTextWatcher)

        sectionNameInput.addTextChangedListener(sectionNameTextWatcher)

        sectionLyricsInput.addTextChangedListener(
            InputTextChangedListener { newText ->
                sectionLyrics[selectedTab.text.toString().trim()] = newText
            })

        songSectionTabLayout.addOnTabSelectedListener(
            ItemSelectedTabListener { tab ->
                selectedTab = tab!!

                sectionNameTextWatcher.ignoreBeforeTextChanged = true

                when (val tabText = tab.text.toString().trim()) {
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
        if (songTitle.isBlank()) {
            return NameValidationState.EMPTY
        }

        val alreadyInUse = intentSong?.title != songTitle && songTitles.contains(songTitle)

        return if (alreadyInUse) {
            NameValidationState.ALREADY_IN_USE
        } else {
            NameValidationState.VALID
        }
    }

    private fun saveSong(): Boolean {
        val title = songTitleInput.text.toString().trim()

        if (validateSongTitle(title) != NameValidationState.VALID) {
            songTitleInput.setText(title)
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

        val selectedCategory = categorySpinner.selectedItem as Category
        val categoryId = selectedCategory.categoryId

        val song = Song(intentSong?.songId, title, categoryId)
        val lyricsSections = sectionLyrics.filter { mapEntry -> mapEntry.key != addText }
            .map { LyricsSection(null, song.id, it.key, it.value) }

        val order = presentation.mapIndexed { index, sectionName -> sectionName to index }

        val songWithLyricsSections = SongWithLyricsSections(song, lyricsSections)
        runBlocking { databaseViewModel.upsertSong(songWithLyricsSections, order) }

        return true
    }

    private fun loadSongData(song: Song) {
        val songWithLyrics: SongWithLyricsSections = runBlocking {
            repository.getSongWithLyrics(song.id)!!
        }


        val songTitleInput: TextInputLayout = findViewById(R.id.tv_song_title)
        songTitleInput.editText!!.setText(song.title)

        val presentation = songWithLyrics.crossRef
            .sorted()
            .map { crossRef -> crossRef.lyricsSectionId }

        songSectionTabLayout.removeAllTabs()

        val lyricsTextMap = songWithLyrics.lyricsSectionsToTextMap()
        val sectionNameMap = songWithLyrics.lyricsSectionsToNameMap()
        for (sectionId in presentation) {
            val sectionName = sectionNameMap[sectionId]!!
            val newTab = songSectionTabLayout.newTab()
            addTab(newTab, sectionName)

            sectionLyricsInput.setText(sectionName)
            newTab.text = sectionName
            sectionLyrics[sectionName] = lyricsTextMap[sectionId]!!
        }

        val newAddTab = songSectionTabLayout.newTab()
        addTab(newAddTab)
        newAddTab.text = getString(R.string.button_add)

        val sectionLyricsInput = findViewById<EditText>(R.id.tin_section_lyrics)
        sectionLyricsInput.setText(lyricsTextMap[presentation.first()]!!)
        sectionNameInput.setText(sectionNameMap[presentation.first()]!!)
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
            val newText = s.toString().trim()

            when (validateSongTitle(newText)) {
                NameValidationState.EMPTY -> {
                    songTitleInputLayout.error = " "
                    songTitleInput.error = getString(R.string.enter_song_title)
                }
                NameValidationState.ALREADY_IN_USE -> {
                    songTitleInputLayout.error = " "
                    songTitleInput.error = getString(R.string.song_title_already_used)
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

            val oldText = s.toString().trim()
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

            val newText = s.toString().trim()
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