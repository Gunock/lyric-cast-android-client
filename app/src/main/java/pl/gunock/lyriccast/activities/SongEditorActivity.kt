/*
 * Created by Tomasz Kiljanczyk on 4/8/21 1:47 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/8/21 1:41 PM
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

    private var mIntentSong: Song? = null
    private lateinit var mRepository: LyricCastRepository
    private val mDatabaseViewModel: DatabaseViewModel by viewModels {
        DatabaseViewModelFactory(baseContext, (application as LyricCastApplication).repository)
    }

    private lateinit var mSectionNameInput: EditText
    private lateinit var mSongTitleInputLayout: TextInputLayout
    private lateinit var mSongTitleInput: EditText
    private lateinit var mSectionLyricsInput: EditText
    private lateinit var mSongSectionTabLayout: TabLayout
    private lateinit var mSelectedTab: TabLayout.Tab
    private lateinit var mCategorySpinner: Spinner

    private val mSectionNameTextWatcher: SectionNameTextWatcher = SectionNameTextWatcher()
    private val mSongTitleTextWatcher: SongTitleTextWatcher = SongTitleTextWatcher()

    private lateinit var mSongTitles: Set<String>
    private val mSectionLyrics: MutableMap<String, String> = mutableMapOf()
    private val mTabCountMap: MutableMap<String, Int> = mutableMapOf()

    private var mNewSectionCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_song_editor)
        setSupportActionBar(findViewById(R.id.toolbar_main))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mRepository = (application as LyricCastApplication).repository

        mSongTitleInputLayout = findViewById(R.id.tv_song_title)
        mSongTitleInput = findViewById(R.id.tin_song_title)
        mSectionNameInput = findViewById(R.id.tin_section_name)
        mSectionLyricsInput = findViewById(R.id.tin_section_lyrics)
        mSongSectionTabLayout = findViewById(R.id.tbl_song_section)
        mCategorySpinner = findViewById(R.id.spn_song_editor_category)

        mSongTitleInput.filters = arrayOf(InputFilter.LengthFilter(30))
        mSectionNameInput.filters = arrayOf(InputFilter.AllCaps(), InputFilter.LengthFilter(30))

        mDatabaseViewModel.allSongs.observe(this) { songs ->
            mSongTitles = songs.map { songAndCategory -> songAndCategory.song.title }.toSet()
        }
        setupCategorySpinner()

        mIntentSong = intent.getParcelableExtra("song")
        Log.v(TAG, "Received song : $mIntentSong")
        if (mIntentSong != null) {
            loadSongData(mIntentSong!!)

            mSelectedTab = mSongSectionTabLayout.getTabAt(0)!!
        } else {
            mSongTitleInput.setText("")
            mSelectedTab = mSongSectionTabLayout.getTabAt(0)!!
            val sectionName = mSelectedTab.text.toString().trim()
            mSectionLyrics[sectionName] = ""
            mTabCountMap[sectionName] = 1
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
        val categorySpinnerAdapter = CategorySpinnerAdapter(baseContext)
        mCategorySpinner.adapter = categorySpinnerAdapter

        mDatabaseViewModel.allCategories.observe(this) { categories ->
            categorySpinnerAdapter.submitCollection(categories, Category.NONE)

            if (mIntentSong != null) {
                val categoryIndex = (mCategorySpinner.adapter as CategorySpinnerAdapter).categories
                    .map { category -> category.categoryId }
                    .indexOf(mIntentSong!!.categoryId)

                mCategorySpinner.setSelection(categoryIndex)
            }
        }
    }

    private fun setupListeners() {
        mSongTitleInput.addTextChangedListener(mSongTitleTextWatcher)

        mSectionNameInput.addTextChangedListener(mSectionNameTextWatcher)

        mSectionLyricsInput.addTextChangedListener(
            InputTextChangedListener { newText ->
                mSectionLyrics[mSelectedTab.text.toString().trim()] = newText
            })

        mSongSectionTabLayout.addOnTabSelectedListener(
            ItemSelectedTabListener { tab ->
                mSelectedTab = tab!!

                mSectionNameTextWatcher.ignoreBeforeTextChanged = true

                when (val tabText = tab.text.toString().trim()) {
                    getString(R.string.editor_button_add) -> {
                        if (mSongSectionTabLayout.tabCount <= 1) {
                            return@ItemSelectedTabListener
                        }

                        mSectionLyricsInput.setText("")

                        val newSectionText = getString(R.string.song_editor_input_new_section)
                        while (
                            mTabCountMap.keys.any { sectionName ->
                                sectionName.contains(newSectionText)
                                        && sectionName.split(" ")
                                    .last() == mNewSectionCount.toString()
                            }
                        ) {
                            mNewSectionCount++
                        }

                        val newTabText =
                            getString(R.string.song_editor_input_new_section_template).format(
                                mNewSectionCount
                            )
                        mSectionNameInput.setText(newTabText)
                        mNewSectionCount++

                        val newAddTab = mSongSectionTabLayout.newTab()
                        newAddTab.text = getString(R.string.editor_button_add)
                        addTab(newAddTab)
                    }
                    else -> {
                        mSectionNameTextWatcher.ignoreOnTextChanged = true
                        mSectionNameInput.setText(tabText)
                        mSectionLyricsInput.setText(mSectionLyrics[tabText])
                    }
                }
            }
        )

        findViewById<ImageButton>(R.id.btn_move_section_left).setOnClickListener {
            mSongSectionTabLayout.moveTabLeft(mSelectedTab)
        }

        findViewById<ImageButton>(R.id.btn_move_section_right).setOnClickListener {
            mSongSectionTabLayout.moveTabRight(mSelectedTab)
        }

        findViewById<Button>(R.id.btn_delete_section).setOnClickListener {
            removeTab(mSelectedTab)
        }
    }

    private fun validateSongTitle(songTitle: String): NameValidationState {
        if (songTitle.isBlank()) {
            return NameValidationState.EMPTY
        }

        val alreadyInUse = mIntentSong?.title != songTitle && mSongTitles.contains(songTitle)

        return if (alreadyInUse) {
            NameValidationState.ALREADY_IN_USE
        } else {
            NameValidationState.VALID
        }
    }

    private fun saveSong(): Boolean {
        val title = mSongTitleInput.text.toString().trim()

        if (validateSongTitle(title) != NameValidationState.VALID) {
            mSongTitleInput.setText(title)
            mSongTitleInput.requestFocus()
            return false
        }

        val addText = getString(R.string.editor_button_add)

        val presentation: MutableList<String> = mutableListOf()
        for (position in 0 until mSongSectionTabLayout.tabCount) {
            val tab = mSongSectionTabLayout.getTabAt(position)!!
            if (tab.text == addText) {
                continue
            }
            presentation.add(tab.text.toString())
        }

        val selectedCategory = mCategorySpinner.selectedItem as Category
        val categoryId = selectedCategory.categoryId

        val song = Song(mIntentSong?.songId, title, categoryId)
        val lyricsSections = mSectionLyrics.filter { mapEntry -> mapEntry.key != addText }
            .map { LyricsSection(null, song.id, it.key, it.value) }

        val order = presentation.mapIndexed { index, sectionName -> sectionName to index }

        val songWithLyricsSections = SongWithLyricsSections(song, lyricsSections)
        runBlocking { mDatabaseViewModel.upsertSong(songWithLyricsSections, order) }

        return true
    }

    private fun loadSongData(song: Song) {
        val songWithLyrics: SongWithLyricsSections = runBlocking {
            mRepository.getSongWithLyrics(song.id)!!
        }


        val songTitleInput: TextInputLayout = findViewById(R.id.tv_song_title)
        songTitleInput.editText!!.setText(song.title)

        val presentation = songWithLyrics.crossRef
            .sorted()
            .map { crossRef -> crossRef.lyricsSectionId }

        mSongSectionTabLayout.removeAllTabs()

        val lyricsTextMap = songWithLyrics.lyricsSectionsToTextMap()
        val sectionNameMap = songWithLyrics.lyricsSectionsToNameMap()
        for (sectionId in presentation) {
            val sectionName = sectionNameMap[sectionId]!!
            val newTab = mSongSectionTabLayout.newTab()
            addTab(newTab, sectionName)

            mSectionLyricsInput.setText(sectionName)
            newTab.text = sectionName
            mSectionLyrics[sectionName] = lyricsTextMap[sectionId]!!
        }

        val newAddTab = mSongSectionTabLayout.newTab()
        addTab(newAddTab)
        newAddTab.text = getString(R.string.editor_button_add)

        val sectionLyricsInput = findViewById<EditText>(R.id.tin_section_lyrics)
        sectionLyricsInput.setText(lyricsTextMap[presentation.first()]!!)
        mSectionNameInput.setText(sectionNameMap[presentation.first()]!!)
    }

    private fun addTab(tab: TabLayout.Tab, tabText: String = "") {
        mSongSectionTabLayout.addTab(tab)

        if (tabText.isBlank()) {
            return
        }

        if (mTabCountMap.containsKey(tabText)) {
            mTabCountMap[tabText] = mTabCountMap[tabText]!! + 1
        } else {
            mTabCountMap[tabText] = 1
        }
    }

    private fun removeTab(tab: TabLayout.Tab) {
        val tabText = tab.text.toString()

        if (tabText == getString(R.string.editor_button_add)) {
            return
        }

        val tabCount = mTabCountMap[tabText]!! - 1

        if (tabCount <= 0) {
            mTabCountMap.remove(tabText)
            mSectionLyrics.remove(tabText)
        } else {
            mTabCountMap[tabText] = tabCount
        }

        if (mSongSectionTabLayout.tabCount > 2) {
            mSongSectionTabLayout.removeTab(tab)
        } else {
            mNewSectionCount = 1
            val newTabText =
                getString(R.string.song_editor_input_new_section_template).format(mNewSectionCount)

            mTabCountMap[tabText] = 1
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
                    mSongTitleInputLayout.error = " "
                    mSongTitleInput.error = getString(R.string.song_editor_enter_title)
                }
                NameValidationState.ALREADY_IN_USE -> {
                    mSongTitleInputLayout.error = " "
                    mSongTitleInput.error = getString(R.string.song_editor_title_already_used)
                }
                NameValidationState.VALID -> {
                    mSongTitleInputLayout.error = null
                    mSongTitleInput.error = null
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
            val oldTabCount = mTabCountMap[oldText]!!
            if (oldTabCount <= 1) {
                mSectionLyrics.remove(oldText)
                mTabCountMap.remove(oldText)
            } else {
                mTabCountMap[oldText] = oldTabCount - 1
            }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (ignoreOnTextChanged) {
                ignoreOnTextChanged = false
                return
            }

            val newText = s.toString().trim()
            mSelectedTab.text = newText

            mTabCountMap[newText] =
                if (mTabCountMap.containsKey(newText)) mTabCountMap[newText]!! + 1 else 1

            if (mSectionLyrics.containsKey(newText)) {
                mSectionLyricsInput.setText(mSectionLyrics[newText]!!)
            } else {
                mSectionLyrics[newText] = mSectionLyricsInput.text.toString()
            }
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }
}