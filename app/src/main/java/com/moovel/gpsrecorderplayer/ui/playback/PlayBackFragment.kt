package com.moovel.gpsrecorderplayer.ui.playback

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moovel.gpsrecorderplayer.R
import com.moovel.gpsrecorderplayer.repo.Record
import com.moovel.gpsrecorderplayer.ui.MainActivity
import kotlinx.android.synthetic.main.playback_fragment.*

class PlayBackFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.playback_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).enableBackButton(true)
        val record = arguments?.getParcelable<Record>("record")
        record_name.setText(record?.name)
    }
}
