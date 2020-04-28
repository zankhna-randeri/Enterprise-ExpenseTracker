package com.avengers.enterpriseexpensetracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.adapter.viewholder.NotificationViewHolder
import com.avengers.enterpriseexpensetracker.modal.Notification

class NotificationAdapter(private var notifications: List<Notification>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        lateinit var viewHolder: RecyclerView.ViewHolder
        val inflater = LayoutInflater.from(parent.context)
        val reqViewHolder = inflater.inflate(R.layout.item_notification, parent, false)
        viewHolder = NotificationViewHolder(reqViewHolder)
        return viewHolder
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NotificationViewHolder) {
            holder.getDateView().text = notifications[position].getDate()
            holder.getMessageView().text = notifications[position].getNotificationMessage()
        }
    }

    fun getNotifications(): List<Notification> {
        return notifications
    }
}
