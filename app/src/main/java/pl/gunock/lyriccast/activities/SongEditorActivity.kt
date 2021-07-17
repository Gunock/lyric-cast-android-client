/*
 * Created by Tomasz Kiljanczyk on 17/07/2021, 11:19
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 17/07/2021, 10:50
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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import io.realm.RealmList
import io.realm.RealmResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bson.types.ObjectId
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.adapters.spinner.CategorySpinnerAdapter
import pl.gunock.lyriccast.common.extensions.moveTabLeft
import pl.gunock.lyriccast.common.extensions.moveTabRight
import pl.gunock.lyriccast.databinding.ActivitySongEditorBinding
import pl.gunock.lyriccast.databinding.ContentSongEditorBinding
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.datamodel.documents.CategoryDocument
import pl.gunock.lyriccast.datamodel.documents.SongDocument
import pl.gunock.lyriccast.datamodel.documents.embedded.LyricsSectionDocument
import pl.gunock.lyriccast.enums.NameValidationState
import pl.gunock.lyriccast.extensions.loadAd
import pl.gunock.lyriccast.listeners.InputTextChangedListener
import pl.gunock.lyriccast.listeners.ItemSelectedTabListener

class SongEditorActivity : AppCompatActivity() {
    private companion object {
        const val TAG = "SongEditorActivity"
    }

    private var mIntentSong: SongDocument? = null

    private val mDatabaseViewModel: DatabaseViewModel by viewModels {
        DatabaseViewModel.Factory(resources)
    }
    private lateinit var mBinding: ContentSongEditorBinding

    private lateinit var mSelectedTab: TabLayout.Tab

    private val mSectionNameTextWatcher: SectionNameTextWatcher = SectionNameTextWatcher()
    private val mSongTitleTextWatcher: SongTitleTextWatcher = SongTitleTextWatcher()

    private lateinit var mSongTitles: Set<String>
    private val mSectionLyrics: MutableMap<String, String> = mutableMapOf()
    private val mTabCountMap: MutableMap<String, Int> = mutableMapOf()

    private var mNewSectionCount = 1

    private lateinit var mCategoryNone: CategoryDocument

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootBinding = ActivitySongEditorBinding.inflate(layoutInflater)
        mBinding = rootBinding.contentSongEditor
        setContentView(rootBinding.root)
        setSupportActionBar(rootBinding.toolbarMain)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mBinding.advSongEditor.loadAd()

        mCategoryNone = CategoryDocument(name = baseContext.getString(R.string.category_none))

        mBinding.edSongTitle.filters = arrayOf(
            InputFilter.LengthFilter(resources.getInteger(R.integer.ed_max_length_song_title))
        )
        mBinding.edSectionName.filters = arrayOf(
            InputFilter.AllCaps(),
            InputFilter.LengthFilter(resources.getInteger(R.integer.ed_max_length_section_name))
        )

        mDatabaseViewModel.allSongs.addChangeListener { songs ->
            mSongTitles = songs.map { it.title }.toSet()
        }

        val intentSongId: ObjectId? = intent.getSerializableExtra("songId") as ObjectId?
        if (intentSongId != null) {
            mIntentSong = mDatabaseViewModel.getSong(intentSongId)!!
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

    override fun onDestroy() {
        mDatabaseViewModel.close()
        super.onDestroy()
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

    private suspend fun setupCategorySpinner() {
        val categorySpinnerAdapter = CategorySpinnerAdapter(baseContext)
        mBinding.spnSongEditorCategory.adapter = categorySpinnerAdapter

        val categories: RealmResults<CategoryDocument> = mDatabaseViewModel.allCategories

        categorySpinnerAdapter.submitCollection(categories, mCategoryNone)

        if (mIntentSong != null) {
            val categoryIndex =
                (mBinding.spnSongEditorCategory.adapter as CategorySpinnerAdapter).categories
                    .map { category -> category.name }
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

        var selectedCategory: CategoryDocument? =
            mBinding.spnSongEditorCategory.selectedItem as CategoryDocument?
        if (selectedCategory?.name == getString(R.string.category_none)) {
            selectedCategory = null
        }

        val lyricsSections = mSectionLyrics.filter { mapEntry -> mapEntry.key != addText }
            .map { LyricsSectionDocument(it.key, it.value) }
            .toTypedArray()

        val song = SongDocument(
            title,
            RealmList(*lyricsSections),
            RealmList(*presentation.toTypedArray()),
            selectedCategory,
            mIntentSong?.id ?: ObjectId()
        )

        mDatabaseViewModel.upsertSong(song)

        return true
    }

    private fun loadSongData(song: SongDocument) {
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