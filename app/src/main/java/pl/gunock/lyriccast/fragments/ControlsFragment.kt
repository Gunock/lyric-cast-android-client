/*
 * Created by Tomasz Kiljańczyk on 2/26/21 9:36 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/26/21 8:22 PM
 */

package pl.gunock.lyriccast.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.google.android.gms.cast.framework.CastContext
import org.json.JSONObject
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.listeners.SessionCreatedListener
import pl.gunock.lyriccast.utils.ControlAction
import pl.gunock.lyriccast.utils.MessageHelper

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ControlsFragment : Fragment() {

    private val args: ControlsFragmentArgs by navArgs()

    private lateinit var songTitleView: TextView
    private lateinit var slideNumberView: TextView
    private lateinit var slidePreviewView: TextView

    private var castContext: CastContext? = null

    private var currentSlide = 0

    private var sessionCreatedListener: SessionCreatedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_controls, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        castContext = CastContext.getSharedInstance()
        sessionCreatedListener = SessionCreatedListener {
            sendSlide()
        }
        castContext?.sessionManager?.addSessionManagerListener(sessionCreatedListener)

        songTitleView = view.findViewById(R.id.text_view_controls_song_title)
        slideNumberView = view.findViewById(R.id.text_view_slide_number)
        slidePreviewView = view.findViewById(R.id.text_view_slide_preview)

        songTitleView.text = args.songTitle

        setViewListeners(view)

        sendSlide()
    }

    override fun onResume() {
        super.onResume()

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val fontSize = prefs.getString("fontSize", "40")
        val backgroundColor = prefs.getString("backgroundColor", "Black")
        val fontColor = prefs.getString("fontColor", "White")

        val configurationJson = JSONObject().apply {
            put("fontSize", "${fontSize}px")
            put("backgroundColor", backgroundColor)
            put("fontColor", fontColor)
        }

        MessageHelper.sendControlMessage(
            castContext!!,
            ControlAction.CONFIGURE,
            configurationJson
        )
    }

    override fun onStop() {
        super.onStop()
        // TODO: Remove unused code
//        if (castContext!!.sessionManager!!.currentSession != null) {
//            castContext!!.sessionManager!!.endCurrentSession(true)
//        }
        castContext?.sessionManager?.removeSessionManagerListener(sessionCreatedListener)
    }

    private fun setViewListeners(view: View) {
        view.findViewById<Button>(R.id.button_control_blank).setOnClickListener {
            sendBlank()
        }

        view.findViewById<Button>(R.id.button_prev).setOnClickListener {
            if (currentSlide <= 0) {
                return@setOnClickListener
            }
            currentSlide--
            sendSlide()
        }

        view.findViewById<Button>(R.id.button_next).setOnClickListener {
            if (currentSlide >= args.lyrics.size - 1) {
                return@setOnClickListener
            }
            currentSlide++
            sendSlide()
        }
    }

    private fun sendBlank() {
        if (castContext == null) {
            return
        }

        MessageHelper.sendControlMessage(castContext!!, ControlAction.BLANK)
    }

    @SuppressLint("SetTextI18n")
    private fun sendSlide() {
        slideNumberView.text = "${currentSlide + 1}/${args.lyrics.size}"
        slidePreviewView.text = args.lyrics[currentSlide]

        if (castContext == null) {
            return
        }

        MessageHelper.sendContentMessage(
            castContext!!,
            args.lyrics[currentSlide]
        )
    }
}