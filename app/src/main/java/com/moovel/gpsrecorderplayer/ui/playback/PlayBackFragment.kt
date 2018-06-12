package com.moovel.gpsrecorderplayer.ui.playback

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.moovel.gpsrecorderplayer.R
import com.moovel.gpsrecorderplayer.repo.Record
import com.moovel.gpsrecorderplayer.ui.MainActivity
import com.moovel.gpsrecorderplayer.utils.dpToPx
import com.moovel.gpsrecorderplayer.utils.latLng
import com.moovel.gpsrecorderplayer.utils.observe
import com.moovel.gpsrecorderplayer.utils.setLocationSource
import kotlinx.android.synthetic.main.playback_fragment.*

class PlayBackFragment : Fragment(), OnMapReadyCallback {
    private lateinit var viewModel: PlayViewModel

    private var googleMap: GoogleMap? = null

    private val record: Record? get() = arguments?.getParcelable("record")

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
        record_name.setText(record?.name)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        share_button.setOnClickListener {  } // TODO export current record
        delete_button.setOnClickListener {  } // TODO delete current record
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(PlayViewModel::class.java)

        viewModel.location.observe(this) { location ->
            location_view.location = location
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location.latLng, 17f))
        }

        viewModel.signal.observe(this) { signal -> location_view.signal = signal }

        play_button.setOnClickListener {
            val r = record ?: return@setOnClickListener

            if (viewModel.playing.value == true) {
                viewModel.stop()
            } else {
                viewModel.play(r)
            }
        }

        viewModel.playing.observe(this) { playing ->
            play_button.setImageDrawable(requireContext().getDrawable(when (playing) {
                true -> R.drawable.ic_stop_white_24dp
                else -> R.drawable.ic_play_arrow_white_24dp
            }))
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.setPadding(0, 0, 0, 56.dpToPx())
        googleMap.setLocationSource(viewModel.location)
        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.setAllGesturesEnabled(false)
        googleMap.uiSettings.isMyLocationButtonEnabled = false
    }
}
