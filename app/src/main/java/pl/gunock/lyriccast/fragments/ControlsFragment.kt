/*
 * Created by Tomasz Kiljańczyk on 10/14/20 11:51 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/13/20 8:55 PM
 */

package pl.gunock.lyriccast.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.cast.framework.CastContext
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.listeners.SessionCreatedListener
import pl.gunock.lyriccast.utils.ControlAction
import pl.gunock.lyriccast.utils.MessageHelper

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ControlsFragment : Fragment() {

    private var castContext: CastContext? = null
    private var slidePreview: TextView? = null
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
        castContext!!.sessionManager!!.addSessionManagerListener(sessionCreatedListener)
        slidePreview = view.findViewById(R.id.text_view_slide_preview)

        view.findViewById<Button>(R.id.button_control_blank).setOnClickListener {
            MessageHelper.sendControlMessage(castContext!!, ControlAction.BLANK)
        }

        view.findViewById<Button>(R.id.button_prev).setOnClickListener {
            SongsContext.previousSlide()
            sendSlide()
        }

        view.findViewById<Button>(R.id.button_next).setOnClickListener {
            SongsContext.nextSlide()
            sendSlide()
        }

        sendSlide()
    }

    override fun onStop() {
        super.onStop()
        if (castContext!!.sessionManager!!.currentSession != null) {
            castContext!!.sessionManager!!.endCurrentSession(true)
        }
        castContext!!.sessionManager!!.removeSessionManagerListener(sessionCreatedListener)
    }

    private fun sendSlide() {
        slidePreview!!.text = SongsContext.getCurrentSlide()
        MessageHelper.sendContentMessage(
            castContext!!,
            SongsContext.getCurrentSlide()
        )
    }
}