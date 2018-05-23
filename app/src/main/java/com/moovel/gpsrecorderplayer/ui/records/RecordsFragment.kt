package com.moovel.gpsrecorderplayer.ui.records

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
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
            NavHostFragment.findNavController(this).navigate(R.id.record_fragment)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(RecordsViewModel::class.java)

        viewModel.records.observe(this, Observer<List<Record>> {
            recordsAdapter.submitList(it)
        })
    }
}
