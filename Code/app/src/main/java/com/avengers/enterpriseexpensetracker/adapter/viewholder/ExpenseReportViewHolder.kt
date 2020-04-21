package com.avengers.enterpriseexpensetracker.adapter.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.avengers.enterpriseexpensetracker.R

class ExpenseReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var txtDate = itemView.findViewById<TextView>(R.id.txt_date)
    private var txtName = itemView.findViewById<TextView>(R.id.txt_name)
    private var txtAmount = itemView.findViewById<TextView>(R.id.txt_amount)

    fun getDateView(): TextView {
        return txtDate
    }

    fun getNameView(): TextView {
        return txtName
    }

    fun getAmountView(): TextView {
        return txtAmount
    }
}