/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 17:30
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 17:29
 */

package pl.gunock.lyriccast.ui.setlist_controls

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.application.getCastConfigurationJson
import pl.gunock.lyriccast.databinding.ActivitySetlistControlsBinding
import pl.gunock.lyriccast.databinding.ContentSetlistControlsBinding
import pl.gunock.lyriccast.shared.cast.CastMessageHelper
import pl.gunock.lyriccast.shared.cast.CustomMediaRouteActionProvider
import pl.gunock.lyriccast.shared.extensions.getSettings
import pl.gunock.lyriccast.shared.extensions.loadAd
import pl.gunock.lyriccast.ui.settings.SettingsActivity
import pl.gunock.lyriccast.ui.shared.listeners.ClickAdapterItemListener
import pl.gunock.lyriccast.ui.shared.listeners.LongClickAdapterItemListener

// TODO: Check if AndroidX SelectionTracker can be used
@AndroidEntryPoint
class SetlistControlsActivity : AppCompatActivity() {

    private val viewModel: SetlistControlsModel by viewModels()

    private lateinit var binding: ContentSetlistControlsBinding

    private lateinit var songItemsAdapter: ControlsSongItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootBinding = ActivitySetlistControlsBinding.inflate(layoutInflater)
        binding = rootBinding.contentSetlistControls
        setContentView(rootBinding.root)
        setSupportActionBar(rootBinding.toolbarControls)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding.advSetlistControls.loadAd()

        val sessionsManager: SessionManager = CastContext.getSharedInstance()!!.sessionManager
        viewModel.initialize(sessionsManager)
        viewModel.castConfiguration = getSettings().getCastConfigurationJson()

        val setlistId: String = intent.getStringExtra("setlistId")!!
        viewModel.loadSetlist(setlistId)

        CastMessageHelper.isBlanked
            .onEach { blanked ->
                if (blanked) {
                    binding.btnSetlistBlank.setBackgroundColor(getColor(R.color.red))
                    binding.btnSetlistBlank.setText(R.string.controls_off)
                } else {
                    binding.btnSetlistBlank.setBackgroundColor(getColor(R.color.green))
                    binding.btnSetlistBlank.setText(R.string.controls_on)
                }
            }
            .flowOn(Dispatchers.Main)
            .launchIn(lifecycleScope)

        viewModel.currentSlideText
            .onEach { binding.tvSetlistSlidePreview.text = it }
            .launchIn(lifecycleScope)

        viewModel.currentSongTitle
            .onEach { binding.tvCurrentSongTitle.text = it }
            .launchIn(lifecycleScope)

        viewModel.currentSongPosition
            .onEach {
                binding.rcvSongs.scrollToPosition(it)
                binding.rcvSongs.postInvalidate()
            }.launchIn(lifecycleScope)

        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()

        val settings = getSettings()
        viewModel.castConfiguration = settings.getCastConfigurationJson()

        if (settings.controlButtonsHeight > 0.0) {
            val params = binding.setlistControlsButtonContainer.layoutParams
            params.height = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                settings.controlButtonsHeight,
                resources.displayMetrics
            ).toInt()
            binding.setlistControlsButtonContainer.layoutParams = params
        }

        viewModel.sendConfiguration()
        viewModel.sendSlide()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_controls, menu)

        val castActionProvider =
            MenuItemCompat.getActionProvider(menu.findItem(R.id.menu_cast)) as CustomMediaRouteActionProvider
        castActionProvider.routeSelector = CastContext.getSharedInstance()!!.mergedSelector!!

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> goToSettings()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        binding.rcvSongs.setHasFixedSize(true)
        binding.rcvSongs.layoutManager = LinearLayoutManager(baseContext)

        val onLongClickListener =
            LongClickAdapterItemListener { _: ControlsSongItemsAdapter.ViewHolder, position, _ ->
                binding.rcvSongs.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                viewModel.selectSong(position)
                return@LongClickAdapterItemListener true
            }

        val onClickListener =
            ClickAdapterItemListener { _: ControlsSongItemsAdapter.ViewHolder, position, _ ->
                binding.rcvSongs.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                viewModel.selectSong(position)
            }

        songItemsAdapter = ControlsSongItemsAdapter(
            this,
            viewModel.songs,
            onItemLongClickListener = onLongClickListener,
            onItemClickListener = onClickListener
        )

        binding.rcvSongs.adapter = songItemsAdapter

        viewModel.changedSongPositions
            .onEach { itemPositions ->
                itemPositions.forEach { songItemsAdapter.notifyItemChanged(it) }
            }.launchIn(lifecycleScope)
    }

    private fun setupListeners() {
        binding.btnSetlistBlank.setOnClickListener { viewModel.sendBlank() }
        binding.btnSetlistPrev.setOnClickListener { viewModel.previousSlide() }
        binding.btnSetlistNext.setOnClickListener { viewModel.nextSlide() }
    }

    private fun goToSettings(): Boolean {
        val intent = Intent(baseContext, SettingsActivity::class.java)
        startActivity(intent)
        return true
    }
}