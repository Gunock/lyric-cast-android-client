/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.ui.setlist_editor.songs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.thomas_kiljanczyk.lyriccast.databinding.FragmentSongsBinding
import dev.thomas_kiljanczyk.lyriccast.domain.models.CategoryItem
import dev.thomas_kiljanczyk.lyriccast.shared.extensions.hideKeyboard
import dev.thomas_kiljanczyk.lyriccast.ui.shared.adapters.CategorySpinnerAdapter
import dev.thomas_kiljanczyk.lyriccast.ui.shared.adapters.SongItemsAdapter
import dev.thomas_kiljanczyk.lyriccast.ui.shared.listeners.InputTextChangedListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class SetlistEditorSongsFragment : Fragment() {

    private val args: SetlistEditorSongsFragmentArgs by navArgs()
    private val viewModel: SetlistEditorSongsModel by activityViewModels()
    private lateinit var binding: FragmentSongsBinding

    private lateinit var songItemsAdapter: SongItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.init()

        setupMenu()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSongsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val setlistSongIds = args.presentation.toList()
        lifecycleScope.launch(Dispatchers.Default) {
            viewModel.updateSetlistSongIds(setlistSongIds)
        }

        setupRecyclerView()
        setupCategorySpinner()
        setupListeners()
        view.hideKeyboard()
    }

    override fun onPause() {
        super.onPause()

        requireView().hideKeyboard()
    }

    private fun setupMenu() {
        val menuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(SetlistSongsMenuProvider(), this)
    }

    private fun setupListeners() {
        binding.edSongTitleFilter.addTextChangedListener(InputTextChangedListener { newText ->
            viewModel.searchValues.songTitle = newText
        })

        binding.edSongTitleFilter.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                view.hideKeyboard()
            }
        }

        binding.swtSelectedSongs.setOnCheckedChangeListener { _, isChecked ->
            viewModel.searchValues.isSelected = if (isChecked) true else null
        }
    }

    private fun setupCategorySpinner() {
        val categorySpinnerAdapter = CategorySpinnerAdapter(binding.dropdownCategory.context)

        binding.dropdownCategory.setAdapter(categorySpinnerAdapter)
        binding.dropdownCategory.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val categoryItem = categorySpinnerAdapter.getItem(position) as CategoryItem?
                val categoryId: String? = categoryItem?.category?.id
                viewModel.searchValues.categoryId = categoryId
            }

        viewModel.categories
            .onEach {
                categorySpinnerAdapter.submitCollection(it)

                val firstCategoryName = categorySpinnerAdapter.getItem(0).category.name
                withContext(Dispatchers.Main) {
                    binding.dropdownCategory.setText(firstCategoryName)
                }
            }
            .flowOn(Dispatchers.Main)
            .launchIn(lifecycleScope)
    }

    private fun setupRecyclerView() {
        songItemsAdapter = SongItemsAdapter(binding.rcvSongs.context)
        songItemsAdapter.onItemClickListener = {
            if (it != null) {
                val position = viewModel.selectSong(it)
                songItemsAdapter.notifyItemChanged(position, true)
            }
        }

        binding.rcvSongs.setHasFixedSize(true)
        binding.rcvSongs.layoutManager = LinearLayoutManager(requireContext())
        binding.rcvSongs.adapter = songItemsAdapter

        viewModel.songs
            .onEach { songItemsAdapter.submitList(it) }
            .launchIn(lifecycleScope)
    }

    private fun goToSetlistFragment() {
        val presentation = viewModel.updatePresentation(args.presentation)

        val action = SetlistEditorSongsFragmentDirections
            .actionSetlistEditorSongsToSetlistEditor(
                setlistId = args.setlistId,
                presentation = presentation.toTypedArray(),
                setlistName = args.setlistName
            )

        findNavController().navigate(action)
    }


    private inner class SetlistSongsMenuProvider : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                android.R.id.home -> {
                    goToSetlistFragment()
                    return true
                }

                else -> false
            }
        }
    }

}
