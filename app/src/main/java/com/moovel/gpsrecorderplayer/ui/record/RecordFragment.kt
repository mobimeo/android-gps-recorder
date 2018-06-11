package com.moovel.gpsrecorderplayer.ui.record

import android.annotation.SuppressLint
import android.location.Location
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
import com.moovel.gpsrecorderplayer.R
import com.moovel.gpsrecorderplayer.repo.Signal
import com.moovel.gpsrecorderplayer.ui.MainActivity
import com.moovel.gpsrecorderplayer.utils.dpToPx
import com.moovel.gpsrecorderplayer.utils.latLng
import com.moovel.gpsrecorderplayer.utils.observe
import com.moovel.gpsrecorderplayer.utils.setLocationSource
import kotlinx.android.synthetic.main.record_fragment.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_DATE

class RecordFragment : Fragment(), OnMapReadyCallback {
    private lateinit var viewModel: RecordViewModel
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.record_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity().enableBackButton(true)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        edit_record_name.setText(getString(R.string.record_new_record, LocalDate.now().format(ISO_DATE), 1))
        edit_record_name.requestFocus()
        edit_record_name.setSelection(edit_record_name.text.length)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.setPadding(0, 0, 0, 56.dpToPx())
        googleMap.setLocationSource(viewModel.locationLiveData)
        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.setAllGesturesEnabled(false)
        googleMap.uiSettings.isMyLocationButtonEnabled = false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(RecordViewModel::class.java)
        viewModel.locationLiveData.observe(this, Observer<Location> { location ->
            if (location == null) return@Observer
            location_view.location = location
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location.latLng, 17f))
        })

        viewModel.signalLiveData.observe(this, Observer<Signal> { signal -> location_view.signal = signal })

        record_button.setOnClickListener { viewModel.onClickButton(edit_record_name.editableText.toString()) }
        viewModel.recordingLiveData.observe(this, Observer<Boolean> { recording ->
            record_button.setImageDrawable(requireContext().getDrawable(when (recording) {
                true -> R.drawable.ic_stop_white_24dp
                else -> R.drawable.ic_fiber_manual_record_white_24dp
            }))
        })

        viewModel.stopListener = { record ->
            if (record != null) {
                mainActivity().startPlaybackFragment(Bundle().apply { putParcelable("record", record) })
            } else {
                // TODO handle record failed
            }
        }

        viewModel.tickerLiveData.observe(this) {
            it?.let { timer.text = DateUtils.formatElapsedTime(it) }
            timer.visibility = if (it == null) GONE else VISIBLE
        }
    }

    private fun mainActivity() = (activity as MainActivity)
}
