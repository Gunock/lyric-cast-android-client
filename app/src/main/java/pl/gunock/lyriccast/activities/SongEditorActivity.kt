/*
 * Created by Tomasz Kiljańczyk on 10/25/20 10:05 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/25/20 9:37 PM
 */

package pl.gunock.lyriccast.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputLayout
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.listeners.InputTextChangeListener
import pl.gunock.lyriccast.listeners.TabItemSelectedListener

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

        findViewById<TabLayout>(R.id.tab_layout_song_section).addOnTabSelectedListener(
            TabItemSelectedListener {
                selectedTab = it

                if (it!!.text == getString(R.string.add)) {
                    sectionNameInput!!.editText!!.setText(getString(R.string.new_section))

                    sectionLyrics[it] = ""

                    val newAddTab = it.parent!!.newTab()
                    newAddTab.text = getString(R.string.add)
                    it.parent!!.addTab(newAddTab)
                } else {
                    sectionNameInput!!.editText!!.setText(it.text)
                    findViewById<EditText>(R.id.text_input_section_lyrics).setText(sectionLyrics[it])
                }
            })
    }

}