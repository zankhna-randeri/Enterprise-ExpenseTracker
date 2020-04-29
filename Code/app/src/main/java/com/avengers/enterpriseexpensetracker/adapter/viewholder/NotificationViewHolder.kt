package com.avengers.enterpriseexpensetracker.adapter.viewholder

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.adapter.RecyclerClickListener
import com.avengers.enterpriseexpensetracker.util.Constants
import java.lang.ref.WeakReference

class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private var txtDate = itemView.findViewById<TextView>(R.id.txt_date)
    private var txtMessage = itemView.findViewById<TextView>(R.id.txt_notification_message)
    private var notificationItemView = itemView.findViewById<CardView>(R.id.notificationItemView)
    private var itemClickListener: WeakReference<RecyclerClickListener>? = null

    constructor(itemView: View, clickListener: RecyclerClickListener) : this(itemView) {
        itemClickListener = WeakReference(clickListener)
        notificationItemView.setOnClickListener(this)
    }

    fun getMessageView(): TextView {
        return txtMessage
    }

    fun getDateView(): TextView {
        return txtDate
    }

    override fun onClick(v: View?) {
        Log.d(Constants.TAG, "Adapter position on click: $adapterPosition")
        when (v?.id) {
            notificationItemView.id -> {
                itemClickListener?.get()?.onItemClickListener(adapterPosition)
            }
        }
    }
}
