/*
 * Created by Tomasz Kiljanczyk on 04/10/2021, 18:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 04/10/2021, 18:29
 */

package pl.gunock.lyriccast.ui.main.setlists

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.gunock.lyriccast.common.extensions.normalize
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.domain.models.SetlistItem
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class SetlistsViewModel @Inject constructor(
    private val setlistsRepository: SetlistsRepository,
    val dataTransferRepository: DataTransferRepository
) : ViewModel() {
    private companion object {
        const val TAG: String = "SetlistsViewModel"
    }

    val setlists: LiveData<List<SetlistItem>>
        get() = _setlists

    private val _setlists: MutableLiveData<List<SetlistItem>> = MutableLiveData(listOf())

    val pickedItem: LiveData<SetlistItem>
        get() = _pickedItem

    private val _pickedItem: MutableLiveData<SetlistItem> = MutableLiveData()


    val numberOfSelectedItems: LiveData<Int>
        get() = _numberOfSelectedItems

    private val _numberOfSelectedItems: MutableLiveData<Int> = MutableLiveData(0)


    private var allSetlists: Iterable<SetlistItem> = listOf()

    val selectionTracker: SelectionTracker<BaseViewHolder> =
        SelectionTracker(this::onItemSelection)

    private var setlistsSubscription: Disposable? = null

    init {
        setlistsSubscription = setlistsRepository.getAllSetlists()
            .subscribe {
                viewModelScope.launch(Dispatchers.Default) {
                    val setlistItems = it.map { setlist -> SetlistItem(setlist) }
                    allSetlists = setlistItems
                    _setlists.postValue(setlistItems)
                }
            }
    }

    override fun onCleared() {
        setlistsSubscription?.dispose()
        super.onCleared()
    }

    suspend fun deleteSelected() {
        val selectedSetlists = allSetlists.filter { item -> item.isSelected.value!! }
            .map { item -> item.setlist.id }

        setlistsRepository.deleteSetlists(selectedSetlists)
    }

    // TODO: Move filter to separate class (functional interface?)
    suspend fun filter(setlistName: String) {
        withContext(Dispatchers.Default) {
            val normalizedTitle = setlistName.trim().normalize()
            val duration = measureTimeMillis {
                val filteredItems = allSetlists.filter { setlistItem ->
                    setlistItem.normalizedName.contains(normalizedTitle, ignoreCase = true)
                }.toSortedSet().toList()

                _setlists.postValue(filteredItems)
            }
            Log.v(TAG, "Filtering took : ${duration}ms")
        }
    }

    private fun onItemSelection(
        @Suppress("UNUSED_PARAMETER")
        holder: BaseViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        val item = _setlists.value!![position]

        if (!isLongClick && selectionTracker.count == 0) {
            _pickedItem.postValue(item)
        } else {
            item.isSelected.postValue(!item.isSelected.value!!)
            _numberOfSelectedItems.postValue(selectionTracker.countAfter)
        }

        return true
    }

    fun resetSelection() {
        _setlists.value?.forEach { it.isSelected.postValue(false) }
        selectionTracker.reset()
    }
}