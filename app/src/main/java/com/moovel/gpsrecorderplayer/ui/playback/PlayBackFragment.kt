/**
 * Copyright (c) 2010-2018 Moovel Group GmbH - moovel.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.moovel.gpsrecorderplayer.ui.playback

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_MOCK_LOCATION
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.text.format.DateUtils
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
import com.moovel.gpsrecorderplayer.BuildConfig
import com.moovel.gpsrecorderplayer.R
import com.moovel.gpsrecorderplayer.repo.Record
import com.moovel.gpsrecorderplayer.ui.BackPressable
import com.moovel.gpsrecorderplayer.ui.DeleteDialog
import com.moovel.gpsrecorderplayer.ui.MainActivity
import com.moovel.gpsrecorderplayer.utils.dpToPx
import com.moovel.gpsrecorderplayer.utils.latLng
import com.moovel.gpsrecorderplayer.utils.observe
import com.moovel.gpsrecorderplayer.utils.setLocationSource
import com.moovel.gpsrecorderplayer.utils.zoomToPolyline
import kotlinx.android.synthetic.main.playback_fragment.*

class PlayBackFragment : Fragment(), OnMapReadyCallback, DeleteDialog.Callback, BackPressable {
    private lateinit var viewModel: PlayViewModel

    private var googleMap: GoogleMap? = null
    private var polyline: Polyline? = null
    private val record: Record? get() = arguments?.getParcelable("record")

    private val mainActivity: MainActivity? get() = activity as? MainActivity

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

        back_button.setOnClickListener {
            viewModel.stop()
            mainActivity?.startRecordsFragment()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(PlayViewModel::class.java)

        record?.let { viewModel.initialize(it) }

        viewModel.location.observe(this) { location ->
            location_view.location = location
            val cameraUpdate = location?.latLng?.let { CameraUpdateFactory.newLatLngZoom(it, 17f) }
            cameraUpdate?.let { googleMap?.moveCamera(it) }
        }

        viewModel.signal.observe(this) { signal -> location_view.signal = signal }

        play_button.setOnClickListener {
            when {
                !hasLocationPermission() -> {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0x2F6A)
                }
                !isMockApp() -> {
                    // TODO better to show dialog with instructions
                    showDevOptions()
                }
                viewModel.playing.value == true -> viewModel.stop()
                else -> viewModel.play()
            }
        }

        viewModel.playing.observe(this) { playing ->
            play_button.setImageDrawable(requireContext().getDrawable(when (playing) {
                true -> R.drawable.ic_stop_white_24dp
                else -> R.drawable.ic_play_arrow_white_24dp
            }))
        }

        viewModel.polyline.observe(this, ::updatePolyline)

        viewModel.tickerLiveData.observe(this) {
            it?.let { timer.text = DateUtils.formatElapsedTime(it) }
            timer.visibility = if (it == null) View.GONE else View.VISIBLE
        }
    }

    override fun onDelete() {
        record?.let { viewModel.remove(it) }
        mainActivity?.startRecordsFragment()
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

    private fun updatePolyline(points: List<LatLng>?) {
        val map = googleMap ?: return
        polyline?.remove()
        polyline = null
        points?.let {
            polyline = map.addPolyline(PolylineOptions().addAll(it))
            map.zoomToPolyline(it)
        }
    }

    override fun onBackPressed(): Boolean {
        viewModel.stop()
        return false
    }

    private fun hasLocationPermission() =
            requireActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun isMockApp(): Boolean {
        val opsManager = requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val uid = Process.myUid()
        return opsManager.checkOpNoThrow(OPSTR_MOCK_LOCATION, uid, BuildConfig.APPLICATION_ID) == MODE_ALLOWED
    }

    private fun showDevOptions() {
        startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
    }
}
