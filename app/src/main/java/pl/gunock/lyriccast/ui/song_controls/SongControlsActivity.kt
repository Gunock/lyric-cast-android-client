/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 17:30
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 16:51
 */

package pl.gunock.lyriccast.ui.song_controls

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.cast.framework.CastContext
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.ActivitySongControlsBinding
import pl.gunock.lyriccast.databinding.ContentSongControlsBinding
import pl.gunock.lyriccast.shared.cast.CustomMediaRouteActionProvider
import pl.gunock.lyriccast.shared.extensions.getSettings
import pl.gunock.lyriccast.shared.extensions.loadAd
import pl.gunock.lyriccast.ui.settings.SettingsActivity

@AndroidEntryPoint
class SongControlsActivity : AppCompatActivity() {

    private val viewModel: SongControlsModel by viewModels()

    private lateinit var binding: ContentSongControlsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootBinding = ActivitySongControlsBinding.inflate(layoutInflater)
        setContentView(rootBinding.root)
        setSupportActionBar(rootBinding.toolbarControls)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding = ContentSongControlsBinding.bind(rootBinding.contentSongControls.root)
        binding.advSongControls.loadAd()

        viewModel.settings = getSettings()

        val songId: String = intent.getStringExtra("songId")!!
        viewModel.loadSong(songId)
        binding.tvControlsSongTitle.text = viewModel.songTitle

        viewModel.currentBlankTextAndColor
            .onEach {
                val (blankText: Int, blankColor: Int) = it
                binding.btnSongBlank.setBackgroundColor(getColor(blankColor))
                binding.btnSongBlank.text = getString(blankText)
            }.flowOn(Dispatchers.Main)
            .launchIn(lifecycleScope)

        viewModel.currentSlideText
            .onEach { binding.tvSlidePreview.text = it }
            .flowOn(Dispatchers.Main)
            .launchIn(lifecycleScope)

        viewModel.currentSlideNumber
            .onEach { binding.tvSongSlideNumber.text = it }
            .flowOn(Dispatchers.Main)
            .launchIn(lifecycleScope)

        setupListeners()
    }

    override fun onResume() {
        super.onResume()

        val settings = getSettings()
        viewModel.settings = settings

        if (settings.controlButtonsHeight > 0.0) {
            val params = binding.songControlsButtonContainer.layoutParams
            params.height = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                settings.controlButtonsHeight,
                resources.displayMetrics
            ).toInt()
            binding.songControlsButtonContainer.layoutParams = params
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

    private fun setupListeners() {
        binding.btnSongBlank.setOnClickListener { viewModel.sendBlank() }
        binding.btnSongPrev.setOnClickListener { viewModel.previousSlide() }
        binding.btnSongNext.setOnClickListener { viewModel.nextSlide() }
    }

    private fun goToSettings(): Boolean {
        val intent = Intent(baseContext, SettingsActivity::class.java)
        startActivity(intent)
        viewModel.sendConfiguration()
        return true
    }

}