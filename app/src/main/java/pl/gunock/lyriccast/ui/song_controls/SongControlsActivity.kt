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
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.datastore.core.DataStore
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.cast.framework.CastContext
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.application.AppSettings
import pl.gunock.lyriccast.databinding.ActivitySongControlsBinding
import pl.gunock.lyriccast.databinding.ContentSongControlsBinding
import pl.gunock.lyriccast.shared.cast.CastMessageHelper
import pl.gunock.lyriccast.shared.cast.CustomMediaRouteActionProvider
import pl.gunock.lyriccast.ui.settings.SettingsActivity
import javax.inject.Inject

@AndroidEntryPoint
class SongControlsActivity : AppCompatActivity() {

    private val viewModel: SongControlsModel by viewModels()

    @Inject
    lateinit var dataStore: DataStore<AppSettings>

    private lateinit var binding: ContentSongControlsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val rootBinding = ActivitySongControlsBinding.inflate(layoutInflater)
        setContentView(rootBinding.root)
        setSupportActionBar(rootBinding.toolbarControls)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding = ContentSongControlsBinding.bind(rootBinding.contentSongControls.root)

        val sessionsManager = CastContext.getSharedInstance()!!.sessionManager
        viewModel.initialize(sessionsManager)

        val songId: String = intent.getStringExtra("songId")!!
        viewModel.loadSong(songId)
        binding.tvControlsSongTitle.text = viewModel.songTitle

        CastMessageHelper.isBlanked
            .onEach { blanked ->
                if (blanked) {
                    binding.btnSongBlank.setBackgroundColor(getColor(R.color.red))
                    binding.btnSongBlank.setText(R.string.controls_off)
                } else {
                    binding.btnSongBlank.setBackgroundColor(getColor(R.color.green))
                    binding.btnSongBlank.setText(R.string.controls_on)
                }
            }
            .flowOn(Dispatchers.Main)
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

        setOnApplyWindowInsetsListener(rootBinding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            rootBinding.toolbarControls.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
                bottomMargin = 0
            }

            binding.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom
            }

            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onResume() {
        super.onResume()

        CoroutineScope(Dispatchers.Main).launch {
            val settings = dataStore.data.first()
            if (settings.controlButtonsHeight > 0.0) {
                val params = binding.songControlsButtonContainer.layoutParams
                params.height = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    settings.controlButtonsHeight,
                    resources.displayMetrics
                ).toInt()

                binding.songControlsButtonContainer.layoutParams = params
            }
        }
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
            R.id.menu_settings -> {
                goToSettings()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupListeners() {
        binding.btnSongBlank.setOnClickListener { viewModel.sendBlank() }
        binding.btnSongPrev.setOnClickListener { viewModel.previousSlide() }
        binding.btnSongNext.setOnClickListener { viewModel.nextSlide() }
    }

    private fun goToSettings() {
        val intent = Intent(baseContext, SettingsActivity::class.java)
        startActivity(intent)
    }

}