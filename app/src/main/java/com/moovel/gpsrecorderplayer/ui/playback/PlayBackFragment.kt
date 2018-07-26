package com.moovel.gpsrecorderplayer.ui.playback

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast.LENGTH_LONG
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.moovel.gpsrecorderplayer.R
import com.moovel.gpsrecorderplayer.repo.Record
import com.moovel.gpsrecorderplayer.ui.DeleteDialog
import com.moovel.gpsrecorderplayer.ui.MainActivity
import com.moovel.gpsrecorderplayer.utils.dpToPx
import com.moovel.gpsrecorderplayer.utils.latLng
import com.moovel.gpsrecorderplayer.utils.observe
import com.moovel.gpsrecorderplayer.utils.setLocationSource
import com.moovel.gpsrecorderplayer.utils.zoomToPolyline
import kotlinx.android.synthetic.main.playback_fragment.*

class PlayBackFragment : Fragment(), OnMapReadyCallback, DeleteDialog.Callback {

    private lateinit var viewModel: PlayViewModel

    private var googleMap: GoogleMap? = null
    private var polyline: Polyline? = null
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
        share_button.setOnClickListener {
            record?.let {
                viewModel.export(it) { intent, cause ->
                    if (cause != null) {
                        // FIXME improvement & lifecycle
                        Snackbar.make(container, cause.message ?: cause.toString(), LENGTH_LONG).show()
                    }

                    if (intent != null) {
                        // FIXME lifecycle
                        if (intent.resolveActivity(requireContext().packageManager) != null) {
                            startActivity(Intent.createChooser(intent, null))
                        }
                    }
                }
            }
        }
        delete_button.setOnClickListener {
            DeleteDialog.instance(R.string.playback_delete_prompt).show(childFragmentManager, "delete")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(PlayViewModel::class.java)

        record?.let { viewModel.initialize(it) }

        viewModel.location.observe(this) { location ->
            location_view.location = location
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location.latLng, 17f))
        }

        viewModel.signal.observe(this) { signal -> location_view.signal = signal }

        play_button.setOnClickListener {
            if (viewModel.playing.value == true) {
                viewModel.stop()
            } else {
                viewModel.play()
            }
        }

        viewModel.playing.observe(this) { playing ->
            play_button.setImageDrawable(requireContext().getDrawable(when (playing) {
                true -> R.drawable.ic_stop_white_24dp
                else -> R.drawable.ic_play_arrow_white_24dp
            }))
        }

        viewModel.polyline.observe(this) {
            updatePolyline(it)
        }
    }

    override fun onDelete() {
        record?.let { viewModel.remove(it) }
        mainActivity().startRecordsFragment()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.setPadding(0, 0, 0, 56.dpToPx())
        googleMap.setLocationSource(viewModel.location)
        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.setAllGesturesEnabled(false)
        googleMap.uiSettings.isMyLocationButtonEnabled = false
        viewModel.polyline.value?.let {
            if (isResumed) updatePolyline(it)
        }
    }

    private fun updatePolyline(points: List<LatLng>) {
        val map = googleMap ?: return
        polyline?.points = points

        if (polyline == null) {
            polyline = map.addPolyline(PolylineOptions().addAll(points))
        }
        map.zoomToPolyline(points)
    }

    private fun mainActivity() = (activity as MainActivity)
}
