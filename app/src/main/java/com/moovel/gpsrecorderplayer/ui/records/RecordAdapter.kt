package com.moovel.gpsrecorderplayer.ui.records

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moovel.gpsrecorderplayer.R
import com.moovel.gpsrecorderplayer.repo.Record
import com.moovel.gpsrecorderplayer.utils.primaryTextColor
import kotlinx.android.synthetic.main.record.view.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle.MEDIUM
import java.util.TimeZone

class RecordAdapter : ListAdapter<Record, RecordAdapter.RecordViewHolder>(DIFF) {
    var clickListener: ((Record) -> Unit)? = null
    private val selectedLiveData = MutableLiveData<Set<Record>>()
    private val selectedRecords = HashSet<Record>()

    val selection: Set<Record> get() = selectedRecords

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

    inner class ClearDiff(val selected: Collection<Record>) : DiffUtil.Callback() {
        override fun areItemsTheSame(old: Int, new: Int): Boolean = old == new

        override fun getOldListSize(): Int = itemCount

        override fun getNewListSize(): Int = itemCount

        override fun areContentsTheSame(old: Int, new: Int): Boolean = !selected.contains(getItem(new))
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

    fun clearSelection() {
        val diff = DiffUtil.calculateDiff(ClearDiff(selectedRecords), false)
        selectedRecords.clear()
        diff.dispatchUpdatesTo(this)
        selectedLiveData.value = selectedRecords
    }

    fun selectedLiveData(): LiveData<Set<Record>> = selectedLiveData

    private fun toggleSelection(position: Int) {
        val record = getItem(position)
        if (selectedRecords.contains(record)) selectedRecords.remove(record) else selectedRecords.add(record)
        notifyItemChanged(position)
        selectedLiveData.value = selectedRecords
    }

    class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(record: Record, selected: Boolean) {
            val ctx = itemView.context
            val time = LocalDateTime.ofInstant(Instant.ofEpochMilli(record.start), TimeZone.getDefault().toZoneId())
            val textColor = if (selected) ctx.getColor(R.color.selectedText) else ctx.primaryTextColor()

            itemView.name.text = record.name
            itemView.name.setTextColor(textColor)
            itemView.created.text = DateTimeFormatter.ofLocalizedDateTime(MEDIUM).format(time)
            itemView.created.setTextColor(textColor)
            itemView.setBackgroundColor(ctx.getColor(if (selected) R.color.colorPrimary else R.color.background))
            itemView.icon.setImageDrawable(ctx.getDrawable(
                    if (selected) R.drawable.ic_check_primary_24dp else R.drawable.ic_location_on_white_24dp))
            itemView.icon.background = ctx.getDrawable(
                    if (selected) R.drawable.list_circle_white else R.drawable.list_circle_primary)
        }
    }
}

