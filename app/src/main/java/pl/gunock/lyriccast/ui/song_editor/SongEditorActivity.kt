/*
 * Created by Tomasz Kiljanczyk on 03/10/2021, 12:04
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 03/10/2021, 11:55
 */

package pl.gunock.lyriccast.ui.song_editor

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.common.extensions.moveTabLeft
import pl.gunock.lyriccast.common.extensions.moveTabRight
import pl.gunock.lyriccast.databinding.ActivitySongEditorBinding
import pl.gunock.lyriccast.databinding.ContentSongEditorBinding
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.shared.enums.NameValidationState
import pl.gunock.lyriccast.shared.extensions.loadAd
import pl.gunock.lyriccast.ui.shared.adapters.CategorySpinnerAdapter
import pl.gunock.lyriccast.ui.shared.listeners.InputTextChangedListener
import pl.gunock.lyriccast.ui.shared.listeners.ItemSelectedTabListener
import javax.inject.Inject

@AndroidEntryPoint
class SongEditorActivity : AppCompatActivity() {
    private companion object {
        const val TAG = "SongEditorActivity"
    }

    @Inject
    lateinit var songsRepository: SongsRepository

    @Inject
    lateinit var categoriesRepository: CategoriesRepository

    private var mIntentSong: Song? = null

    private lateinit var mBinding: ContentSongEditorBinding

    private lateinit var mSelectedTab: TabLayout.Tab

    private val mSectionNameTextWatcher: SectionNameTextWatcher = SectionNameTextWatcher()
    private val mSongTitleTextWatcher: SongTitleTextWatcher = SongTitleTextWatcher()

    private lateinit var mSongTitles: Set<String>
    private val mSectionLyrics: MutableMap<String, String> = mutableMapOf()
    private val mTabCountMap: MutableMap<String, Int> = mutableMapOf()

    private var mNewSectionCount = 1

    private lateinit var mCategoryNone: Category

    private var mSongsSubscription: Disposable? = null
    private var mCategoriesSubscription: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootBinding = ActivitySongEditorBinding.inflate(layoutInflater)
        mBinding = rootBinding.contentSongEditor
        setContentView(rootBinding.root)
        setSupportActionBar(rootBinding.toolbarMain)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mBinding.advSongEditor.loadAd()

        mCategoryNone = Category(name = baseContext.getString(R.string.category_none))

        mBinding.edSongTitle.filters = arrayOf(
            InputFilter.LengthFilter(resources.getInteger(R.integer.ed_max_length_song_title))
        )
        mBinding.edSectionName.filters = arrayOf(
            InputFilter.AllCaps(),
            InputFilter.LengthFilter(resources.getInteger(R.integer.ed_max_length_section_name))
        )

        val intentSongId: String? = intent.getStringExtra("songId")
        if (intentSongId != null) {
            mIntentSong = songsRepository.getSong(intentSongId)!!
        }

        Log.v(TAG, "Received song : $mIntentSong")
        if (mIntentSong != null) {
            loadSongData(mIntentSong!!)

            mSelectedTab = mBinding.tblSongSection.getTabAt(0)!!
        } else {
            mBinding.edSongTitle.setText("")
            mSelectedTab = mBinding.tblSongSection.getTabAt(0)!!
            val sectionName = mSelectedTab.text.toString().trim()
            mSectionLyrics[sectionName] = ""
            mTabCountMap[sectionName] = 1
        }

        lifecycleScope.launch(Dispatchers.Main) { setupCategorySpinner() }
        setupListeners()
    }

    override fun onResume() {
        super.onResume()

        mSongsSubscription = songsRepository.getAllSongs().subscribe { songs ->
            mSongTitles = songs.map { it.title }.toSet()
        }

        mCategoriesSubscription = categoriesRepository.getAllCategories().subscribe { categories ->
            lifecycleScope.launch {
                val categorySpinnerAdapter =
                    mBinding.spnSongEditorCategory.adapter as CategorySpinnerAdapter
//                categorySpinnerAdapter.submitCollection(categories, mCategoryNone)
            }
        }
    }

    override fun onPause() {
        super.onPause()

        mSongsSubscription?.dispose()
        mSongsSubscription = null

        mCategoriesSubscription?.dispose()
        mCategoriesSubscription = null
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
        mBinding.spnSongEditorCategory.adapter = categorySpinnerAdapter

        if (mIntentSong != null) {
            val categoryIndex =
                (mBinding.spnSongEditorCategory.adapter as CategorySpinnerAdapter).items
                    .map { category -> category.category.name }
                    .indexOf(mIntentSong!!.category?.name)

            mBinding.spnSongEditorCategory.setSelection(categoryIndex)
        }
    }

    private fun setupListeners() {
        mBinding.edSongTitle.addTextChangedListener(mSongTitleTextWatcher)

        mBinding.edSectionName.addTextChangedListener(mSectionNameTextWatcher)

        mBinding.edSectionLyrics.addTextChangedListener(
            InputTextChangedListener { newText ->
                mSectionLyrics[mSelectedTab.text.toString().trim()] = newText
            })

        mBinding.tblSongSection.addOnTabSelectedListener(
            ItemSelectedTabListener { tab ->
                mSelectedTab = tab!!

                mSectionNameTextWatcher.ignoreBeforeTextChanged = true

                when (val tabText = tab.text.toString().trim()) {
                    getString(R.string.editor_button_add) -> {
                        if (mBinding.tblSongSection.tabCount <= 1) {
                            return@ItemSelectedTabListener
                        }

                        mBinding.edSectionLyrics.setText("")

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
                        mBinding.edSectionName.setText(newTabText)
                        mNewSectionCount++

                        val newAddTab = mBinding.tblSongSection.newTab()
                        newAddTab.text = getString(R.string.editor_button_add)
                        addTab(newAddTab)
                    }
                    else -> {
                        mSectionNameTextWatcher.ignoreOnTextChanged = true
                        mBinding.edSectionName.setText(tabText)
                        mBinding.edSectionLyrics.setText(mSectionLyrics[tabText])
                    }
                }
            }
        )

        mBinding.btnMoveSectionLeft.setOnClickListener {
            mBinding.tblSongSection.moveTabLeft(mSelectedTab)
        }

        mBinding.btnMoveSectionRight.setOnClickListener {
            mBinding.tblSongSection.moveTabRight(mSelectedTab)
        }

        mBinding.btnDeleteSection.setOnClickListener {
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
        val title = mBinding.edSongTitle.text.toString().trim()

        if (validateSongTitle(title) != NameValidationState.VALID) {
            mBinding.edSongTitle.setText(title)
            mBinding.edSongTitle.requestFocus()
            return false
        }

        val addText = getString(R.string.editor_button_add)

        val presentation: MutableList<String> = mutableListOf()
        for (position in 0 until mBinding.tblSongSection.tabCount) {
            val tab = mBinding.tblSongSection.getTabAt(position)!!
            if (tab.text == addText) {
                continue
            }
            presentation.add(tab.text.toString().trim())
        }

        var selectedCategory: Category? =
            mBinding.spnSongEditorCategory.selectedItem as Category?
        if (selectedCategory?.name == getString(R.string.category_none)) {
            selectedCategory = null
        }

        val lyricsSections = mSectionLyrics.filter { mapEntry -> mapEntry.key != addText }
            .map { Song.LyricsSection(it.key, it.value) }

        val song = Song(
            mIntentSong?.id ?: "",
            title,
            lyricsSections,
            presentation,
            selectedCategory
        )

        songsRepository.upsertSong(song)

        return true
    }

    private fun loadSongData(song: Song) {
        mBinding.edSongTitle.setText(song.title)

        mBinding.tblSongSection.removeAllTabs()

        val lyricsTextMap = song.lyricsMap
        for (sectionName in song.presentation) {
            val newTab = mBinding.tblSongSection.newTab()
            addTab(newTab, sectionName)

            mBinding.edSectionLyrics.setText(sectionName)
            newTab.text = sectionName
            mSectionLyrics[sectionName] = lyricsTextMap[sectionName]!!
        }

        val newAddTab = mBinding.tblSongSection.newTab()
        addTab(newAddTab)
        newAddTab.text = getString(R.string.editor_button_add)

        mBinding.edSectionLyrics.setText(lyricsTextMap[song.presentation.first()]!!)
        mBinding.edSectionName.setText(song.presentation.first())
    }

    private fun addTab(tab: TabLayout.Tab, tabText: String = "") {
        mBinding.tblSongSection.addTab(tab)

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

        if (mBinding.tblSongSection.tabCount > 2) {
            mBinding.tblSongSection.removeTab(tab)
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
                    mBinding.tinSongTitle.error = getString(R.string.song_editor_enter_title)
                }
                NameValidationState.ALREADY_IN_USE -> {
                    mBinding.tinSongTitle.error = getString(R.string.song_editor_title_already_used)
                }
                NameValidationState.VALID -> {
                    mBinding.tinSongTitle.error = null
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
                mBinding.edSectionLyrics.setText(mSectionLyrics[newText]!!)
            } else {
                mSectionLyrics[newText] = mBinding.edSectionLyrics.text.toString()
            }
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }
}