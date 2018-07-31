package com.moovel.gpsrecorderplayer.ui.record

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.moovel.gpsrecorderplayer.R
import com.moovel.gpsrecorderplayer.repo.Record
import com.moovel.gpsrecorderplayer.ui.BackPressable
import com.moovel.gpsrecorderplayer.ui.MainActivity
import com.moovel.gpsrecorderplayer.utils.dpToPx
import com.moovel.gpsrecorderplayer.utils.latLng
import com.moovel.gpsrecorderplayer.utils.observe
import com.moovel.gpsrecorderplayer.utils.setLocationSource
import com.moovel.gpsrecorderplayer.utils.zoomToPolyline
import kotlinx.android.synthetic.main.record_fragment.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RecordFragment : Fragment(), OnMapReadyCallback, BackPressable, BackDialog.Callback {

    private lateinit var viewModel: RecordViewModel
    private var googleMap: GoogleMap? = null
    private var polyline: Polyline? = null
    private var record: Record? = null

    private val mainActivity: MainActivity? get() = activity as MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.record_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        edit_record_name.setText(getString(R.string.record_new_record, LocalDateTime.now().format(
                DateTimeFormatter.ofPattern(" yyyy'-'MM'-'dd'T'HH':'mm"))))
        edit_record_name.requestFocus()
        edit_record_name.setSelection(edit_record_name.text.length)
        back_button.setOnClickListener { if (!onBackPressed()) mainActivity?.startRecordsFragment() }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.setPadding(0, 0, 0, 56.dpToPx())
        googleMap.setLocationSource(viewModel.locationLiveData)
        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.setAllGesturesEnabled(false)
        googleMap.uiSettings.isMyLocationButtonEnabled = false
        viewModel.polyline.value?.let { if (isResumed) updatePolyline(it) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(RecordViewModel::class.java)
        viewModel.locationLiveData.observe(this) { location ->
            location_view.location = location
            val cameraUpdate = location?.latLng?.let { CameraUpdateFactory.newLatLngZoom(it, 17f) }
            cameraUpdate?.let { googleMap?.moveCamera(it) }
        }

        viewModel.signalLiveData.observe(this) { signal -> location_view.signal = signal }

        record_button.setOnClickListener {
            if (viewModel.isRecording()) {
                viewModel.stop(edit_record_name.editableText.toString())
            } else {
                record = viewModel.start(edit_record_name.editableText.toString())
            }
        }
        viewModel.recordingLiveData.observe(this, Observer<Boolean> { recording ->
            record_button.setImageDrawable(requireContext().getDrawable(when (recording) {
                true -> R.drawable.ic_stop_white_24dp
                else -> R.drawable.ic_fiber_manual_record_white_24dp
            }))
            edit_record_name.isEnabled = !recording
        })

        viewModel.tickerLiveData.observe(this) {
            it?.let { timer.text = DateUtils.formatElapsedTime(it) }
            timer.visibility = if (it == null) GONE else VISIBLE
        }

        viewModel.polyline.observe(this, ::updatePolyline)

        viewModel.recordingLiveData.observe(this) { recording ->
            val r = record
            record = null
            if (r != null && !recording) mainActivity?.startPlaybackFragment(r)
        }
    }

    override fun onBackPressed(): Boolean {
        if (viewModel.isRecording()) {
            BackDialog().show(childFragmentManager, "back")
            return true
        }
        return false
    }

    override fun onStopClicked() {
        viewModel.stop(edit_record_name.editableText.toString())
        mainActivity?.startRecordsFragment()
    }

    private fun updatePolyline(points: List<LatLng>?) {
        val map = googleMap ?: return
        polyline?.remove()
        polyline = null
        points?.let {
            polyline = map.addPolyline(PolylineOptions().addAll(it))
            map.zoomToPolyline(it)
        }
    }
}
