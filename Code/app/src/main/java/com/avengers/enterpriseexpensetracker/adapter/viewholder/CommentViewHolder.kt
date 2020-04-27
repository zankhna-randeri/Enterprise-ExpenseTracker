package com.avengers.enterpriseexpensetracker.adapter.viewholder

import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.avengers.enterpriseexpensetracker.R

class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var commentView: TextView = itemView.findViewById(R.id.rptComment)
    private var commentItemView: CardView = itemView.findViewById(R.id.commentItemView)

    fun getCommentView(): TextView {
        return commentView
    }
}
