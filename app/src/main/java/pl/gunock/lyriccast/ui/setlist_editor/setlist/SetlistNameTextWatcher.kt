/*
 * Created by Tomasz Kiljanczyk on 07/10/2021, 11:16
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 07/10/2021, 11:16
 */

package pl.gunock.lyriccast.ui.setlist_editor.setlist

import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.FragmentSetlistEditorBinding
import pl.gunock.lyriccast.shared.enums.NameValidationState

class SetlistNameTextWatcher(
    resources: Resources,
    private val binding: FragmentSetlistEditorBinding,
    private val viewModel: SetlistEditorModel
) : TextWatcher {

    private val enterNameErrorText = resources.getString(R.string.setlist_editor_enter_name)

    private val nameAlreadyUsedErrorText =
        resources.getString(R.string.setlist_editor_name_already_used)

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable?) {
        val newText = s.toString().trim()

        when (viewModel.validateSetlistName(newText)) {
            NameValidationState.EMPTY -> {
                binding.tinSetlistName.error = enterNameErrorText
            }
            NameValidationState.ALREADY_IN_USE -> {
                binding.tinSetlistName.error = nameAlreadyUsedErrorText
            }
            NameValidationState.VALID -> {
                binding.tinSetlistName.error = null
                viewModel.setlistName = newText
            }
        }
    }
}