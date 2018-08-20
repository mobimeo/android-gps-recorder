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

package com.moovel.gpsrecorderplayer.ui.records

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.snackbar.Snackbar
import com.moovel.gpsrecorderplayer.R
import com.moovel.gpsrecorderplayer.repo.Record
import com.moovel.gpsrecorderplayer.ui.DeleteDialog
import com.moovel.gpsrecorderplayer.ui.MainActivity
import kotlinx.android.synthetic.main.records_fragment.*

class RecordsFragment : Fragment(), DeleteDialog.Callback {

    private lateinit var viewModel: RecordsViewModel

    private val adapter = RecordAdapter()

    private val mainActivity: MainActivity? get() = activity as? MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.records_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        records_list.adapter = adapter
        records_list.layoutManager = LinearLayoutManager(requireContext())

        create_record_button.setOnClickListener {
            if (hasLocationPermission()) {
                mainActivity?.startRecordFragment()
            } else {
                requestPermissions(arrayOf(ACCESS_FINE_LOCATION), 0x5F3E)
            }
        }

        adapter.clickListener = { record -> mainActivity?.startPlaybackFragment(record) }

        menu_button.setOnClickListener { bottom_drawer.open() }

        open_source_menu_item.setOnClickListener {
            startActivity(Intent(context, OssLicensesMenuActivity::class.java))
        }

        adapter.selectedLiveData().observe(this, Observer<Set<Record>> { selectedRecords ->
            val selection = selectedRecords?.isNotEmpty() == true
            menu_button.visibility = if (selection) GONE else VISIBLE
            clear_selection_button.visibility = if (selection) VISIBLE else GONE
            delete_button.visibility = if (selection) VISIBLE else GONE
            share_button.visibility = if (selection) VISIBLE else GONE
        })

        delete_button.setOnClickListener {
            DeleteDialog.instance(R.string.records_delete_prompt).show(childFragmentManager, "delete")
        }
        share_button.setOnClickListener {
            val records = adapter.clearSelection()
            viewModel.export(records) { intent, cause ->
                if (cause != null) {
                    // FIXME improvement & lifecycle
                    val msg = cause.message ?: cause.toString()
                    Snackbar.make(container, msg, Snackbar.LENGTH_LONG).show()
                }

                if (intent != null) {
                    // FIXME lifecycle
                    val chooser = Intent.createChooser(intent, null)
                    if (intent.resolveActivity(requireContext().packageManager) != null) {
                        startActivity(chooser)
                    }
                }
            }
        }
        clear_selection_button.setOnClickListener { adapter.clearSelection() }
    }

    override fun onDelete() {
        viewModel.remove(adapter.clearSelection())
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
        if (hasLocationPermission()) mainActivity?.startRecordFragment()
    }
}
