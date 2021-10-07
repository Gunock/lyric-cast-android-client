/*
 * Created by Tomasz Kiljanczyk on 05/10/2021, 18:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 05/10/2021, 18:43
 */

package pl.gunock.lyriccast.ui.setlist_editor.songs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.gunock.lyriccast.databinding.FragmentSongsBinding
import pl.gunock.lyriccast.domain.models.CategoryItem
import pl.gunock.lyriccast.shared.extensions.hideKeyboard
import pl.gunock.lyriccast.ui.shared.adapters.CategorySpinnerAdapter
import pl.gunock.lyriccast.ui.shared.adapters.SongItemsAdapter
import pl.gunock.lyriccast.ui.shared.listeners.InputTextChangedListener
import pl.gunock.lyriccast.ui.shared.listeners.ItemSelectedSpinnerListener

@AndroidEntryPoint
class SetlistEditorSongsFragment : Fragment() {

    private val args: SetlistEditorSongsFragmentArgs by navArgs()
    private val viewModel: SetlistEditorSongsModel by activityViewModels()
    private lateinit var binding: FragmentSongsBinding

    private lateinit var songItemsAdapter: SongItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
        viewModel // Initializes viewModel

        viewModel.setlistSongIds = args.presentation.toList()
        lifecycleScope.launch(Dispatchers.Default) {
            viewModel.updateSongs()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                goToSetlistFragment()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupListeners() {
        binding.edSongTitleFilter.addTextChangedListener(InputTextChangedListener { newText ->
            lifecycleScope.launch(Dispatchers.Default) {
                val categoryItem = binding.spnCategory.selectedItem as CategoryItem?
                viewModel.filterSongs(newText, categoryItem)
            }
        })

        binding.edSongTitleFilter.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                view.hideKeyboard()
            }
        }

        binding.spnCategory.onItemSelectedListener = ItemSelectedSpinnerListener { _, _ ->
            lifecycleScope.launch(Dispatchers.Default) {
                val songTitle = binding.edSongTitleFilter.editableText.toString()
                val categoryItem = binding.spnCategory.selectedItem as CategoryItem?
                viewModel.filterSongs(songTitle, categoryItem)
            }
        }

        binding.swtSelectedSongs.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch(Dispatchers.Default) {
                val songTitle = binding.edSongTitleFilter.editableText.toString()
                val categoryItem = binding.spnCategory.selectedItem as CategoryItem?
                val isSelected = if (isChecked) true else null

                viewModel.filterSongs(songTitle, categoryItem, isSelected)
            }
        }
    }

    private fun setupCategorySpinner() {
        val categorySpinnerAdapter = CategorySpinnerAdapter(requireContext())
        binding.spnCategory.adapter = categorySpinnerAdapter

        viewModel.categories.observe(viewLifecycleOwner) { categories: List<CategoryItem> ->
            lifecycleScope.launch(Dispatchers.Default) {
                categorySpinnerAdapter.submitCollection(categories)
            }
        }
    }

    private fun setupRecyclerView() {
        songItemsAdapter = SongItemsAdapter(
            requireContext(),
            selectionTracker = viewModel.selectionTracker
        )

        binding.rcvSongs.setHasFixedSize(true)
        binding.rcvSongs.layoutManager = LinearLayoutManager(requireContext())
        binding.rcvSongs.adapter = songItemsAdapter

        viewModel.songs.observe(viewLifecycleOwner) {
            lifecycleScope.launch(Dispatchers.Default) {
                songItemsAdapter.submitCollection(it)
            }
        }

        viewModel.selectedSongPosition.observe(viewLifecycleOwner) {
            songItemsAdapter.notifyItemChanged(it)
        }
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

}
