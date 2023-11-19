/*
 * Created by Tomasz Kiljanczyk on 07/01/2023, 21:51
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 07/01/2023, 21:16
 */

package pl.gunock.lyriccast.ui.song_editor

import android.os.Bundle
import android.text.InputFilter
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.common.extensions.moveTabLeft
import pl.gunock.lyriccast.common.extensions.moveTabRight
import pl.gunock.lyriccast.databinding.ActivitySongEditorBinding
import pl.gunock.lyriccast.databinding.ContentSongEditorBinding
import pl.gunock.lyriccast.domain.models.CategoryItem
import pl.gunock.lyriccast.shared.enums.NameValidationState
import pl.gunock.lyriccast.shared.extensions.loadAd
import pl.gunock.lyriccast.ui.shared.adapters.CategorySpinnerAdapter
import pl.gunock.lyriccast.ui.shared.listeners.InputTextChangedListener
import pl.gunock.lyriccast.ui.shared.listeners.ItemSelectedTabListener

@AndroidEntryPoint
class SongEditorActivity : AppCompatActivity() {
    private val viewModel: SongEditorModel by viewModels()

    private lateinit var binding: ContentSongEditorBinding

    private lateinit var categorySpinnerAdapter: CategorySpinnerAdapter

    private lateinit var selectedTab: TabLayout.Tab

    private val sectionNameTextWatcher: SectionNameTextWatcher by lazy {
        SectionNameTextWatcher(binding, viewModel) { selectedTab.text = it }
    }
    private val songTitleTextWatcher: SongTitleTextWatcher by lazy {
        SongTitleTextWatcher(resources, binding, viewModel)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.navigationBarColor = getColor(R.color.background_3)

        val rootBinding = ActivitySongEditorBinding.inflate(layoutInflater)
        binding = rootBinding.contentSongEditor
        setContentView(rootBinding.root)
        setSupportActionBar(rootBinding.toolbarMain)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding.advSongEditor.loadAd()

        binding.edSongTitle.filters = arrayOf(
            InputFilter.LengthFilter(resources.getInteger(R.integer.ed_max_length_song_title))
        )
        binding.edSectionName.filters = arrayOf(
            InputFilter.AllCaps(),
            InputFilter.LengthFilter(resources.getInteger(R.integer.ed_max_length_section_name))
        )

        loadIntentSong()
        setupCategorySpinner()
        setupListeners()

        viewModel.categories
            .onEach { categories ->
                categorySpinnerAdapter.submitCollection(categories, viewModel.categoryNone)
            }.flowOn(Dispatchers.Default)
            .launchIn(lifecycleScope)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_song_editor, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_save -> {
                if (!checkSongTitleValidity()) {
                    return false
                }

                lifecycleScope.launch(Dispatchers.Default) {
                    saveSong()
                    finish()
                }
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupCategorySpinner() {
        categorySpinnerAdapter = CategorySpinnerAdapter(binding.dropdownCategory.context)

        binding.dropdownCategory.setAdapter(categorySpinnerAdapter)
        binding.dropdownCategory.onItemClickListener = OnCategoryItemClickListener()

        val viewModelCategoryName = viewModel.category?.name ?: viewModel.categoryNone.category.name
        binding.dropdownCategory.setText(viewModelCategoryName)

        val viewModelCategoryColor = viewModel.category?.color
        if (viewModelCategoryColor != null) {
            binding.cardCategoryColor.setCardBackgroundColor(viewModelCategoryColor)
            binding.cardCategoryColor.visibility = View.VISIBLE
        } else {
            binding.cardCategoryColor.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        binding.edSongTitle.addTextChangedListener(songTitleTextWatcher)

        binding.edSectionName.addTextChangedListener(sectionNameTextWatcher)

        binding.edSectionLyrics.addTextChangedListener(InputTextChangedListener { newText ->
            val sectionName = selectedTab.text.toString().trim()
            viewModel.setSectionText(sectionName, newText)
        })

        binding.tblSongSection.addOnTabSelectedListener(ItemSelectedTabListener(this::onTabSelected))

        binding.btnMoveSectionLeft.setOnClickListener {
            binding.tblSongSection.moveTabLeft(selectedTab)
        }

        binding.btnMoveSectionRight.setOnClickListener {
            binding.tblSongSection.moveTabRight(selectedTab)
        }

        binding.btnDeleteSection.setOnClickListener {
            removeTab(selectedTab)
        }
    }

    private fun loadIntentSong() {
        val intentSongId: String? = intent.getStringExtra("songId")
        if (intentSongId != null) {
            viewModel.loadSong(intentSongId)
            setupSongSections()
            selectedTab = binding.tblSongSection.getTabAt(0)!!
        } else {
            binding.edSongTitle.setText("")
            selectedTab = binding.tblSongSection.getTabAt(0)!!
            val sectionName = selectedTab.text.toString().trim()
            viewModel.setupSection(sectionName)
        }
    }

    private fun onTabSelected(tab: TabLayout.Tab?) {
        selectedTab = tab!!

        sectionNameTextWatcher.ignoreBeforeTextChanged = true

        when (val tabText = tab.text.toString().trim()) {
            getString(R.string.editor_button_add) -> {
                if (binding.tblSongSection.tabCount <= 1) {
                    return
                }

                binding.edSectionLyrics.setText("")

                val newSectionName = getString(R.string.song_editor_input_new_section)
                viewModel.calculateNewSectionCount(newSectionName)

                val newTabText = viewModel.newSectionName
                binding.edSectionName.setText(newTabText)

                val newAddTab = binding.tblSongSection.newTab()
                newAddTab.text = getString(R.string.editor_button_add)
                addTab(newAddTab)
            }

            else -> {
                sectionNameTextWatcher.ignoreOnTextChanged = true
                binding.edSectionName.setText(tabText)
                binding.edSectionLyrics.setText(viewModel.getSectionText(tabText))
            }
        }
    }

    private fun checkSongTitleValidity(): Boolean {
        val title = binding.edSongTitle.text.toString().trim()

        if (viewModel.validateSongTitle(title) != NameValidationState.VALID) {
            binding.edSongTitle.setText(title)
            binding.edSongTitle.requestFocus()
            return false
        }

        return true
    }

    private suspend fun saveSong() {
        val addText = getString(R.string.editor_button_add)

        val presentation: MutableList<String> = mutableListOf()
        for (position in 0 until binding.tblSongSection.tabCount) {
            val tab = binding.tblSongSection.getTabAt(position)!!
            if (tab.text == addText) {
                continue
            }
            presentation.add(tab.text.toString().trim())
        }

        viewModel.saveSong(presentation)
    }

    private fun setupSongSections() {
        binding.edSongTitle.setText(viewModel.songTitle)

        binding.tblSongSection.removeAllTabs()

        for (sectionName in viewModel.presentation!!) {
            val newTab = binding.tblSongSection.newTab()
            addTab(newTab, sectionName)

            binding.edSectionLyrics.setText(sectionName)
            newTab.text = sectionName
        }

        val newAddTab = binding.tblSongSection.newTab()
        addTab(newAddTab)
        newAddTab.text = getString(R.string.editor_button_add)

        val sectionName = viewModel.presentation!!.first()
        val sectionText = viewModel.getSectionText(sectionName)

        binding.edSectionLyrics.setText(sectionText)
        binding.edSectionName.setText(sectionName)
    }

    private fun addTab(tab: TabLayout.Tab, tabText: String = "") {
        binding.tblSongSection.addTab(tab)

        if (tabText.isBlank()) {
            return
        }

        viewModel.modifySectionCount(tabText)
    }

    private fun removeTab(tab: TabLayout.Tab) {
        val tabText = tab.text.toString()

        if (tabText == getString(R.string.editor_button_add)) {
            return
        }

        if (viewModel.removeSection(tabText)) {
            binding.tblSongSection.removeTab(tab)
        } else {
            val newSectionName = getString(R.string.song_editor_input_new_section)
            tab.text = newSectionName
            viewModel.increaseSectionCount(newSectionName)
            viewModel.setSectionText(newSectionName, "")
            binding.edSectionLyrics.setText("")
        }
    }


    private inner class OnCategoryItemClickListener : OnItemClickListener {
        override fun onItemClick(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            val categoryItem = parent?.getItemAtPosition(position) as CategoryItem?
            val category = if (categoryItem == viewModel.categoryNone) {
                null
            } else {
                categoryItem?.category
            }

            viewModel.category = category

            if (category?.color != null) {
                binding.cardCategoryColor.setCardBackgroundColor(category.color!!)
                binding.cardCategoryColor.visibility = View.VISIBLE
            } else {
                binding.cardCategoryColor.visibility = View.GONE
            }
        }
    }
}