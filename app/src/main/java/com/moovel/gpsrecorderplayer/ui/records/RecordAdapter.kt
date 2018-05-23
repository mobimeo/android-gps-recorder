package com.moovel.gpsrecorderplayer.ui.records

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moovel.gpsrecorderplayer.R
import com.moovel.gpsrecorderplayer.repo.Record
import kotlinx.android.synthetic.main.record.view.*

class RecordAdapter : ListAdapter<Record, RecordAdapter.RecordViewHolder>(DIFF) {
    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Record>() {
            override fun areItemsTheSame(oldItem: Record, newItem: Record): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Record, newItem: Record): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val viewItem = LayoutInflater.from(parent.context).inflate(R.layout.record, parent, false)
        return RecordViewHolder(viewItem)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = getItem(position)
        holder.bind(record)
    }

    class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(record: Record) {
            itemView.name.text = record.name
            itemView.name.created.text = record.start.toString()
        }
    }
}