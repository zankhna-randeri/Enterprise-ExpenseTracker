package com.avengers.enterpriseexpensetracker.adapter.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.avengers.enterpriseexpensetracker.R

class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var txtDate = itemView.findViewById<TextView>(R.id.txt_date)
    private var txtMessage = itemView.findViewById<TextView>(R.id.txt_notification_message)

    fun getMessageView(): TextView {
        return txtMessage
    }

    fun getDateView(): TextView {
        return txtDate
    }
}
