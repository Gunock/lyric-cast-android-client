/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:07
 */

package dev.thomas_kiljanczyk.lyriccast.ui.song_editor

import android.text.Editable
import android.text.TextWatcher
import dev.thomas_kiljanczyk.lyriccast.databinding.ContentSongEditorBinding

class SectionNameTextWatcher(
    private val binding: ContentSongEditorBinding,
    private val viewModel: SongEditorModel,
    private val changeTabText: (String) -> Unit
) : TextWatcher {

    var ignoreBeforeTextChanged: Boolean = false
    var ignoreOnTextChanged: Boolean = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        if (ignoreBeforeTextChanged) {
            ignoreBeforeTextChanged = false
            return
        }

        val oldText = s.toString().trim()
        viewModel.decreaseSectionCount(oldText)
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (ignoreOnTextChanged) {
            ignoreOnTextChanged = false
            return
        }

        val newText = s.toString().trim()
        changeTabText(newText)

        if (viewModel.increaseSectionCount(newText)) {
            binding.edSectionLyrics.setText(viewModel.getSectionText(newText))
        } else {
            viewModel.setSectionText(newText, binding.edSectionLyrics.text.toString())
        }
    }

    override fun afterTextChanged(s: Editable?) {
    }
}