/*
 * Created by Tomasz Kiljanczyk on 05/10/2021, 18:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 05/10/2021, 18:43
 */

package pl.gunock.lyriccast.ui.setlist_controls

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.cast.framework.CastContext
import dagger.hilt.android.AndroidEntryPoint
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.ActivitySetlistControlsBinding
import pl.gunock.lyriccast.databinding.ContentSetlistControlsBinding
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.shared.cast.CastMessageHelper
import pl.gunock.lyriccast.shared.cast.CustomMediaRouteActionProvider
import pl.gunock.lyriccast.shared.cast.SessionStartedListener
import pl.gunock.lyriccast.shared.extensions.loadAd
import pl.gunock.lyriccast.ui.settings.SettingsActivity
import pl.gunock.lyriccast.ui.shared.listeners.ClickAdapterItemListener
import pl.gunock.lyriccast.ui.shared.listeners.LongClickAdapterItemListener
import javax.inject.Inject

@AndroidEntryPoint
class SetlistControlsActivity : AppCompatActivity() {

    @Inject
    lateinit var setlistsRepository: SetlistsRepository

    private lateinit var mBinding: ContentSetlistControlsBinding

    private lateinit var mSessionStartedListener: SessionStartedListener
    private lateinit var mSongItemsAdapter: ControlsSongItemsAdapter

    private lateinit var mSetlistLyrics: List<String>
    private var mSongTitles: MutableMap<Int, String> = mutableMapOf()
    private var mSongStartPoints: MutableMap<String, Int> = mutableMapOf()
    private var mCurrentLyricsPosition: Int = 0

    private var mBlankOnColor: Int = Int.MIN_VALUE
    private var mBlankOffColor: Int = Int.MIN_VALUE
    private val mCurrentBlankColor: Int
        get() = if (CastMessageHelper.isBlanked) {
            mBlankOffColor
        } else {
            mBlankOnColor
        }

    private lateinit var mBlankOffText: String
    private lateinit var mBlankOnText: String
    private val mCurrentBlankText: String
        get() = if (CastMessageHelper.isBlanked) {
            mBlankOffText
        } else {
            mBlankOnText
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootBinding = ActivitySetlistControlsBinding.inflate(layoutInflater)
        mBinding = rootBinding.contentSetlistControls
        setContentView(rootBinding.root)
        setSupportActionBar(rootBinding.toolbarControls)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mBinding.advSetlistControls.loadAd()

        mBlankOffText = getString(R.string.controls_off)
        mBlankOnText = getString(R.string.controls_on)
        mBlankOnColor = getColor(R.color.green)
        mBlankOffColor = getColor(R.color.red)

        mBinding.btnSetlistBlank.setBackgroundColor(mCurrentBlankColor)
        mBinding.btnSetlistBlank.text = mCurrentBlankText

        mSessionStartedListener = SessionStartedListener {
            sendConfigure()
            sendSlide()
        }

        CastContext.getSharedInstance()!!.sessionManager.addSessionManagerListener(
            mSessionStartedListener
        )

        val setlist: Setlist = setupLyrics()

        setupRecyclerView(setlist.presentation)

        setupListeners()
        setPreview()
    }

    override fun onResume() {
        super.onResume()

        sendConfigure()
        sendSlide()
    }

    override fun onDestroy() {
        CastContext.getSharedInstance()!!.sessionManager.removeSessionManagerListener(
            mSessionStartedListener
        )

//        CastMessageHelper.isBlanked.removeObservers(this)

        super.onDestroy()
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

    private fun setupLyrics(): Setlist {
        val setlistId: String = intent.getStringExtra("setlistId")!!

        val setlist: Setlist = setlistsRepository.getSetlist(setlistId)!!

        var setlistLyricsIndex = 0
        mSetlistLyrics = setlist.presentation
            .flatMapIndexed { index: Int, song: Song ->
                val lyrics = song.lyricsList

                val indexedTitle = "[$index] ${song.title}"
                mSongTitles[setlistLyricsIndex] = indexedTitle
                mSongTitles[setlistLyricsIndex + lyrics.size - 1] = indexedTitle
                mSongStartPoints[indexedTitle] = setlistLyricsIndex

                setlistLyricsIndex += lyrics.size

                return@flatMapIndexed lyrics
            }

        return setlist
    }

    private fun setupRecyclerView(songs: List<Song>) {
        mBinding.rcvSongs.setHasFixedSize(true)
        mBinding.rcvSongs.layoutManager = LinearLayoutManager(baseContext)

        val onLongClickListener =
            LongClickAdapterItemListener { _: ControlsSongItemsAdapter.ViewHolder, position, _ ->
                mBinding.rcvSongs.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                selectSong(position)
            }

        val onClickListener =
            ClickAdapterItemListener { _: ControlsSongItemsAdapter.ViewHolder, position, _ ->
                mBinding.rcvSongs.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                selectSong(position)
            }

        val songItemList: List<SongItem> = songs.map { song -> SongItem(song) }

        mSongItemsAdapter = ControlsSongItemsAdapter(
            this,
            songItemList,
            mOnItemLongClickListener = onLongClickListener,
            mOnItemClickListener = onClickListener
        )

        mBinding.rcvSongs.adapter = mSongItemsAdapter
    }

    private fun setupListeners() {
        mBinding.btnSetlistBlank.setOnClickListener {
            CastMessageHelper.sendBlank(!CastMessageHelper.isBlanked)
        }

//        CastMessageHelper.isBlanked.observe(this) {
//            mBinding.btnSetlistBlank.setBackgroundColor(mCurrentBlankColor)
//            mBinding.btnSetlistBlank.text = mCurrentBlankText
//        }

        mBinding.btnSetlistPrev.setOnClickListener {
            if (mCurrentLyricsPosition <= 0) {
                return@setOnClickListener
            }
            mCurrentLyricsPosition--

            setPreview()
            sendSlide()
        }

        mBinding.btnSetlistNext.setOnClickListener {
            if (mCurrentLyricsPosition >= mSetlistLyrics.size - 1) {
                return@setOnClickListener
            }
            mCurrentLyricsPosition++

            setPreview()
            sendSlide()
        }
    }

    private fun highlightSong(title: String) {
        val songItemPosition: Int = mSongStartPoints.keys
            .indexOfFirst { songTitle -> songTitle == title }

        mSongItemsAdapter.songItems.forEachIndexed { index, songItem ->
            songItem.highlight = index == songItemPosition
        }

        mBinding.rcvSongs.run {
            scrollToPosition(songItemPosition)
            postInvalidate()
        }
    }

    private fun setPreview() {
        if (mSongTitles.containsKey(mCurrentLyricsPosition)) {
            val songTitle = mSongTitles[mCurrentLyricsPosition]!!
            mBinding.tvCurrentSongTitle.text = songTitle.replace("^\\[[0-9]+] ".toRegex(), "")
            highlightSong(songTitle)
        }

        mBinding.tvSetlistSlidePreview.text = mSetlistLyrics[mCurrentLyricsPosition]
    }

    private fun selectSong(position: Int): Boolean {
        val title = mSongItemsAdapter.songItems[position].song.title
        val indexedTitle = "[$position] $title"
        mCurrentLyricsPosition = mSongStartPoints[indexedTitle]!!

        setPreview()
        sendSlide()
        return true
    }

    private fun sendSlide() {
        CastMessageHelper.sendContentMessage(
            mSetlistLyrics[mCurrentLyricsPosition]
        )
    }

    private fun sendConfigure() {
        CastMessageHelper.sendConfiguration()
    }

    private fun goToSettings(): Boolean {
        val intent = Intent(baseContext, SettingsActivity::class.java)
        startActivity(intent)
        sendConfigure()
        return true
    }
}