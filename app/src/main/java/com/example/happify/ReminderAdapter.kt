package com.example.happify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.happify.ReminderAdapter.ReminderViewHolder

class ReminderAdapter(private val reminderList: MutableList<Reminder>) :
    RecyclerView.Adapter<ReminderViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminderList[position]
        holder.bind(reminder)
    }

    override fun getItemCount(): Int {
        return reminderList.size
    }

    fun updateData(newData: List<Reminder>?) {
        reminderList.clear()
        reminderList.addAll(newData!!)
        notifyDataSetChanged()
    }

    class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView
        private val timeTextView: TextView
        private val notesTextView: TextView

        init {
            dateTextView = itemView.findViewById(R.id.dateTextView)
            timeTextView = itemView.findViewById(R.id.timeTextView)
            notesTextView = itemView.findViewById(R.id.notesTextView)
        }

        fun bind(reminder: Reminder) {
            dateTextView.text = reminder.reminderDate
            timeTextView.text = reminder.jam
            notesTextView.text = reminder.notes
        }
    }
}