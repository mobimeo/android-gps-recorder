package com.moovel.gpsrecorderplayer.ui.records

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.moovel.gpsrecorderplayer.R
import com.moovel.gpsrecorderplayer.repo.Record
import com.moovel.gpsrecorderplayer.ui.MainActivity
import kotlinx.android.synthetic.main.records_fragment.*

class RecordsFragment : Fragment() {

    private lateinit var viewModel: RecordsViewModel

    private val adapter = RecordAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.records_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity().enableBackButton(false)

        records_list.adapter = adapter
        records_list.layoutManager = LinearLayoutManager(requireContext())

        create_record_button.setOnClickListener {
            if (hasLocationPermission()) {
                mainActivity().startRecordFragment()
            } else {
                requestPermissions(arrayOf(ACCESS_FINE_LOCATION), 0x5F3E)
            }
        }

        adapter.clickListener = { record ->
            (activity as MainActivity).startPlaybackFragment(Bundle().apply { putParcelable("record", record) })
        }

        menu_button.setOnClickListener { bottom_drawer.open() }

        open_source_menu_item.setOnClickListener {
            startActivity(Intent(context, OssLicensesMenuActivity::class.java)) //FIXME use nav controller
        }

        adapter.selectedLiveData().observe(this, Observer<Set<Record>> { selectedRecords ->
            val selection = selectedRecords?.isNotEmpty() == true
            menu_button.visibility = if (selection) GONE else VISIBLE
            clear_selection_button.visibility = if (selection) VISIBLE else GONE
        })

        clear_selection_button.setOnClickListener { adapter.clearSelection() }
    }

    private fun hasLocationPermission() =
            requireActivity().checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(RecordsViewModel::class.java)

        viewModel.records.observe(this, Observer<List<Record>> { list ->
            adapter.submitList(list)
            empty_view.visibility = if (list?.isNotEmpty() == true) View.GONE else View.VISIBLE
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (hasLocationPermission()) mainActivity().startRecordFragment()
    }

    private fun mainActivity() = (activity as MainActivity)
}
