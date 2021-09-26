/*
 * Created by Tomasz Kiljanczyk on 26/09/2021, 17:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 26/09/2021, 17:19
 */

package pl.gunock.lyriccast.ui.setlist_editor

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.Disposable
import io.realm.RealmList
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.FragmentSetlistEditorBinding
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.shared.enums.NameValidationState
import pl.gunock.lyriccast.shared.extensions.hideKeyboard
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.listeners.TouchAdapterItemListener
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker
import javax.inject.Inject

@AndroidEntryPoint
class SetlistEditorFragment : Fragment() {

    private companion object {
        const val TAG = "SetlistEditorFragment"
    }

    @Inject
    lateinit var songsRepository: SongsRepository

    @Inject
    lateinit var setlistsRepository: SetlistsRepository

    private val mArgs: SetlistEditorFragmentArgs by navArgs()
    private lateinit var mBinding: FragmentSetlistEditorBinding

    private val mSetlistNameTextWatcher: SetlistNameTextWatcher = SetlistNameTextWatcher()

    private var mSetlistId: String = ""
    private lateinit var mSetlistSongs: List<SongItem>
    private var mSongItemsAdapter: SetlistSongItemsAdapter? = null
    private lateinit var mSelectionTracker: SelectionTracker<BaseViewHolder>

    private var mIntentSetlist: Setlist? = null

    private lateinit var mSetlistNames: Set<String>

    private var mToast: Toast? = null

    private var mActionMenu: Menu? = null
    private var mActionMode: ActionMode? = null
    private val mActionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.action_menu_setlist_editor, menu)
            mode.title = ""
            mActionMenu = menu
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            showMenuActions()
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val result = when (item.itemId) {
                R.id.action_menu_delete -> removeSelectedSongs()
                R.id.menu_duplicate -> duplicateSong()
                else -> false
            }

            if (result) {
                mode.finish()
            }

            return result
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            mActionMode = null
            mActionMenu = null
            resetSelection()
        }

    }

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

                override fun isLongPressDragEnabled(): Boolean {
                    return false
                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    holder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val from = holder.absoluteAdapterPosition
                    val to = target.absoluteAdapterPosition

                    val adapter = recyclerView.adapter as SetlistSongItemsAdapter
                    adapter.moveItem(from, to)

                    return true
                }

                override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {}
            }

        ItemTouchHelper(simpleItemTouchCallback)
    }

    private var mSetlistsSubscription: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentSetlistEditorBinding.inflate(inflater)
        return mBinding.root
    }

    override fun onDestroy() {
        itemTouchHelper.attachToRecyclerView(null)
        mSongItemsAdapter!!.removeObservers()
        mSongItemsAdapter = null
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val intentSetlistId =
            requireActivity().intent.getStringExtra("setlistId")
        val intentSetlistPresentation = requireActivity().intent
            .getStringArrayExtra("setlistSongs")

        mBinding.edSetlistName.filters = arrayOf(
            InputFilter.LengthFilter(resources.getInteger(R.integer.ed_max_length_setlist_name))
        )

        if (intentSetlistId != null) {
            mSetlistId = intentSetlistId

            mIntentSetlist = setlistsRepository.getSetlist(intentSetlistId)!!
        }

        val setlistSongs: MutableList<Song> = mutableListOf()
        when {
            mArgs.setlistId != null -> {
                mSetlistId = mArgs.setlistId!!

                mBinding.edSetlistName.setText(mArgs.setlistName)

                for (songId in mArgs.presentation!!) {
                    val song: Song = songsRepository.getSong(songId)!!
                    setlistSongs.add(song)
                }
            }
            intentSetlistId != null -> {
                mBinding.edSetlistName.setText(mIntentSetlist!!.name)
                setlistSongs.addAll(mIntentSetlist!!.presentation)
            }
            intentSetlistPresentation != null -> {
                for (songId in intentSetlistPresentation) {
                    val song: Song = songsRepository.getSong(songId)!!
                    setlistSongs.add(song)
                }
            }
        }

        mSetlistSongs = setlistSongs.map { SongItem(it) }

        setupListeners()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()

        mSetlistsSubscription = setlistsRepository.getAllSetlists().subscribe { setlists ->
            mSetlistNames = setlists.map { setlist -> setlist.name }.toSet()
        }
    }

    override fun onPause() {
        super.onPause()

        mSetlistsSubscription?.dispose()
        mSetlistsSubscription = null
    }

    private fun setupListeners() {
        mBinding.edSetlistName.addTextChangedListener(mSetlistNameTextWatcher)

        mBinding.edSetlistName.setOnFocusChangeListener { view_, hasFocus ->
            if (!hasFocus) {
                view_.hideKeyboard()
            }
        }

        mBinding.btnPickSetlistSongs.setOnClickListener {
            mActionMode?.finish()

            val presentation: Array<String> = mSongItemsAdapter!!.items
                .map { it.song.id }
                .toTypedArray()

            val action = SetlistEditorFragmentDirections.actionSetlistEditorToSetlistEditorSongs(
                setlistId = mSetlistId,
                setlistName = mBinding.edSetlistName.text.toString(),
                presentation = presentation
            )

            findNavController().navigate(action)
        }

        mBinding.btnSaveSetlist.setOnClickListener {
            if (saveSetlist()) {
                requireActivity().finish()
            }
        }
    }

    private fun setupRecyclerView() {
        val onHandleTouchListener =
            TouchAdapterItemListener { holder: SetlistSongItemsAdapter.ViewHolder, view, event ->
                view.requestFocus()
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(holder)
                }

                return@TouchAdapterItemListener true
            }

        mSelectionTracker = SelectionTracker(this::onSetlistClick)
        mSongItemsAdapter = SetlistSongItemsAdapter(
            requireContext(),
            mSetlistSongs.toMutableList(),
            mSelectionTracker = mSelectionTracker,
            mOnHandleTouchListener = onHandleTouchListener
        )

        mBinding.rcvSongs.setHasFixedSize(true)
        mBinding.rcvSongs.layoutManager = LinearLayoutManager(requireContext())
        mBinding.rcvSongs.adapter = mSongItemsAdapter

        itemTouchHelper.attachToRecyclerView(mBinding.rcvSongs)
    }

    private fun onSetlistClick(
        @Suppress("UNUSED_PARAMETER")
        holder: BaseViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        if (isLongClick || mSelectionTracker.count != 0) {
            val item: SongItem = mSongItemsAdapter!!.items[position]
            return selectSong(item)
        }
        return false
    }

    private fun validateSetlistName(name: String): NameValidationState {
        if (name.isBlank()) {
            return NameValidationState.EMPTY
        }

        val isAlreadyInUse = mIntentSetlist?.name != name && mSetlistNames.contains(name)

        return if (isAlreadyInUse) {
            NameValidationState.ALREADY_IN_USE
        } else {
            NameValidationState.VALID
        }
    }

    private fun saveSetlist(): Boolean {
        val setlistName = mBinding.edSetlistName.text.toString().trim()

        if (validateSetlistName(setlistName) != NameValidationState.VALID) {
            mBinding.edSetlistName.setText(setlistName)
            mBinding.tinSetlistName.requestFocus()
            return false
        }

        if (mSetlistSongs.isEmpty()) {
            mToast?.cancel()
            mToast = Toast.makeText(
                requireContext(),
                getString(R.string.setlist_editor_empty_warning),
                Toast.LENGTH_SHORT
            )
            mToast!!.show()
            requireView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            return false
        }

        val presentation: Array<Song> = mSongItemsAdapter!!.items
            .map { it.song }
            .toTypedArray()

        val setlist = Setlist(setlistName, RealmList(*presentation), mSetlistId)

        setlistsRepository.upsertSetlist(setlist)
        Log.i(TAG, "Created setlist: $setlist")
        return true
    }

    private fun selectSong(item: SongItem): Boolean {
        item.isSelected.postValue(!item.isSelected.value!!)

        when (mSelectionTracker.countAfter) {
            0 -> {
                if (mSongItemsAdapter!!.showCheckBox.value!!) {
                    mSongItemsAdapter!!.showCheckBox.postValue(false)
                }
                mActionMode?.finish()
                return false
            }
            1 -> {
                if (!mSongItemsAdapter!!.showCheckBox.value!!) {
                    mSongItemsAdapter!!.showCheckBox.postValue(true)
                }

                if (mActionMode == null) {
                    mActionMode = (requireActivity() as AppCompatActivity).startSupportActionMode(
                        mActionModeCallback
                    )
                }

                showMenuActions()
            }
            2 -> showMenuActions(showDuplicate = false)
        }
        return true
    }

    private fun showMenuActions(showDelete: Boolean = true, showDuplicate: Boolean = true) {
        mActionMenu ?: return
        mActionMenu!!.findItem(R.id.action_menu_delete).isVisible = showDelete
        mActionMenu!!.findItem(R.id.menu_duplicate).isVisible = showDuplicate
    }

    private fun removeSelectedSongs(): Boolean {
        mSongItemsAdapter!!.removeSelectedItems()
        resetSelection()

        return true
    }

    private fun duplicateSong(): Boolean {
        mSongItemsAdapter!!.duplicateSelectedItem()
        resetSelection()

        return true
    }

    private fun resetSelection() {
        if (mSongItemsAdapter!!.showCheckBox.value!!) {
            mSongItemsAdapter!!.showCheckBox.postValue(false)
        }
        mSongItemsAdapter!!.resetSelection()
        showMenuActions(showDelete = false, showDuplicate = false)
    }

    inner class SetlistNameTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            val newText = s.toString().trim()

            when (validateSetlistName(newText)) {
                NameValidationState.EMPTY -> {
                    mBinding.tinSetlistName.error = getString(R.string.setlist_editor_enter_name)
                }
                NameValidationState.ALREADY_IN_USE -> {
                    mBinding.tinSetlistName.error =
                        getString(R.string.setlist_editor_name_already_used)
                }
                NameValidationState.VALID -> {
                    mBinding.tinSetlistName.error = null
                }
            }
        }
    }

}
