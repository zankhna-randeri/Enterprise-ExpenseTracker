package com.avengers.enterpriseexpensetracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.adapter.viewholder.MessageViewHolder
import com.avengers.enterpriseexpensetracker.modal.VoiceMessage

class ConversationAdapter(private var messages: MutableList<VoiceMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val REQUEST = 0
    private val RESPONSE = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        lateinit var viewHolder: RecyclerView.ViewHolder
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            REQUEST -> {
                val reqViewHolder = inflater.inflate(R.layout.item_voice_request, parent, false)
                viewHolder = MessageViewHolder(reqViewHolder)
            }
            RESPONSE -> {
                val resViewHolder = inflater.inflate(R.layout.item_voice_response, parent, false)
                viewHolder = MessageViewHolder(resViewHolder)
            }
        }

        return viewHolder
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MessageViewHolder).getMessageView().text = messages[position].text
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isResponse) {
            RESPONSE
        } else
            REQUEST
    }
}