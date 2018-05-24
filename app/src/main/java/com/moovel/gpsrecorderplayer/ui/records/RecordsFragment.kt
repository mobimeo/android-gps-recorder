package com.moovel.gpsrecorderplayer.ui.records

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import com.moovel.gpsrecorderplayer.MainActivity
import com.moovel.gpsrecorderplayer.R
import com.moovel.gpsrecorderplayer.repo.Record
import kotlinx.android.synthetic.main.records_fragment.*

class RecordsFragment : Fragment() {

    private lateinit var viewModel: RecordsViewModel

    private val recordsAdapter = RecordAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.records_fragment, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        records_list.adapter = recordsAdapter
        records_list.layoutManager = LinearLayoutManager(requireContext())

        create_record_button.setOnClickListener {
            if (hasLocationPermission()) {
                begin()
            } else {
                requestPermissions(arrayOf(ACCESS_FINE_LOCATION), 0x5F3E)
            }
        }

        menu_button.setOnClickListener { drawer_layout.openDrawer(GravityCompat.START) }
    }

    private fun hasLocationPermission() =
            requireActivity().checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(RecordsViewModel::class.java)

        viewModel.records.observe(this, Observer<List<Record>> {
            recordsAdapter.submitList(it)
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (hasLocationPermission()) begin()
    }

    private fun begin() {
        NavHostFragment.findNavController(this).navigate(R.id.record_fragment)
    }
}
