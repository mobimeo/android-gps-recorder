package com.moovel.gpsrecorderplayer.ui.records

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moovel.gpsrecorderplayer.R
import com.moovel.gpsrecorderplayer.repo.Record
import com.moovel.gpsrecorderplayer.utils.primaryTextColor
import kotlinx.android.synthetic.main.record.view.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.TimeZone
import kotlin.collections.HashSet


class RecordAdapter : ListAdapter<Record, RecordAdapter.RecordViewHolder>(DIFF) {
    var clickListener: ((Record) -> Unit)? = null
    private val selectedRecords = HashSet<Record>()

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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.record, parent, false)
        val viewHolder = RecordViewHolder(view)

        view.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (selectedRecords.isEmpty()) {
                clickListener?.invoke(getItem(position))
            } else {
                toggleSelection(position)
            }
        }

        view.setOnLongClickListener {
            val position = viewHolder.adapterPosition
            toggleSelection(position)
            true
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = getItem(position)
        holder.bind(record, selectedRecords.contains(record))
    }

    private fun toggleSelection(position: Int) {
        val record = getItem(position)
        if (selectedRecords.contains(record)) selectedRecords.remove(record) else selectedRecords.add(record)
        notifyItemChanged(position)
    }

    class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(record: Record, selected: Boolean) {
            val ctx = itemView.context
            val time = LocalDateTime.ofInstant(Instant.ofEpochMilli(record.start), TimeZone.getDefault().toZoneId())
            val textColor = if (selected) ctx.getColor(R.color.selectedText) else ctx.primaryTextColor()

            itemView.name.text = record.name
            itemView.name.setTextColor(textColor)
            itemView.created.text = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(time)
            itemView.created.setTextColor(textColor)
            itemView.setBackgroundColor(ctx.getColor(if (selected) R.color.colorPrimary else R.color.background))
        }
    }
}
