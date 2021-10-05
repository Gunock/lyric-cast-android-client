/*
 * Created by Tomasz Kiljanczyk on 05/10/2021, 18:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 05/10/2021, 18:43
 */

package pl.gunock.lyriccast.ui.song_controls

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import com.google.android.gms.cast.framework.CastContext
import dagger.hilt.android.AndroidEntryPoint
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.ActivitySongControlsBinding
import pl.gunock.lyriccast.databinding.ContentSongControlsBinding
import pl.gunock.lyriccast.shared.cast.CustomMediaRouteActionProvider
import pl.gunock.lyriccast.shared.extensions.loadAd
import pl.gunock.lyriccast.ui.settings.SettingsActivity

@AndroidEntryPoint
class SongControlsActivity : AppCompatActivity() {

    private val viewModel: SongControlsViewModel by viewModels()

    private lateinit var binding: ContentSongControlsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootBinding = ActivitySongControlsBinding.inflate(layoutInflater)
        setContentView(rootBinding.root)
        setSupportActionBar(rootBinding.toolbarControls)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding = ContentSongControlsBinding.bind(rootBinding.contentSongControls.root)
        binding.advSongControls.loadAd()

        val songId: String = intent.getStringExtra("songId")!!
        viewModel.loadSong(songId)
        binding.tvControlsSongTitle.text = viewModel.songTitle

        viewModel.currentBlankTextAndColor.observe(this) {
            val (blankText: Int, blankColor: Int) = it
            binding.btnSongBlank.setBackgroundColor(getColor(blankColor))
            binding.btnSongBlank.text = getString(blankText)
        }

        viewModel.currentSlideText.observe(this) { binding.tvSlidePreview.text = it }
        viewModel.currentSlideNumber.observe(this) { binding.tvSongSlideNumber.text = it }

        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        viewModel.sendConfiguration()
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
        binding.btnSongBlank.setOnClickListener {
            viewModel.sendBlank()
        }

        binding.btnSongPrev.setOnClickListener {
            viewModel.previousSlide()
        }

        binding.btnSongNext.setOnClickListener {
            viewModel.nextSlide()
        }
    }

    private fun goToSettings(): Boolean {
        val intent = Intent(baseContext, SettingsActivity::class.java)
        startActivity(intent)
        viewModel.sendConfiguration()
        return true
    }

}