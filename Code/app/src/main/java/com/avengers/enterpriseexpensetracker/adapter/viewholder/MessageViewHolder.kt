package com.avengers.enterpriseexpensetracker.adapter.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.avengers.enterpriseexpensetracker.R

class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

    private var msgView: TextView = itemView.findViewById(R.id.txtMessage)

    fun getMessageView(): TextView {
        return msgView
    }
}