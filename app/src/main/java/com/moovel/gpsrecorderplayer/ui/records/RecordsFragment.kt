package com.moovel.gpsrecorderplayer.ui.records

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moovel.gpsrecorderplayer.R

class RecordsFragment : Fragment() {

    companion object {
        fun newInstance() = RecordsFragment()
    }

    private lateinit var viewModel: RecordsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.records_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(RecordsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
