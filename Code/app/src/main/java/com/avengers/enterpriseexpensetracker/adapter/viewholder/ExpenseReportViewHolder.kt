package com.avengers.enterpriseexpensetracker.adapter.viewholder

import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.adapter.RecyclerClickListener
import java.lang.ref.WeakReference

class ExpenseReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private var txtDate = itemView.findViewById<TextView>(R.id.txt_date)
    private var txtName = itemView.findViewById<TextView>(R.id.txt_name)
    private var txtAmount = itemView.findViewById<TextView>(R.id.txt_amount)
    private var btnDelete = itemView.findViewById<ImageButton>(R.id.btnDelete)
    private var reportCardView = itemView.findViewById<CardView>(R.id.reportCardView)
    private var itemHeaderView = itemView.findViewById<LinearLayoutCompat>(R.id.item_report_header)

    private var buttonClickListener: WeakReference<RecyclerClickListener>? = null

    constructor(itemView: View, buttonClickListener: RecyclerClickListener) : this(itemView) {
        this.buttonClickListener = WeakReference(buttonClickListener)
        btnDelete.setOnClickListener(this)
        reportCardView.setOnClickListener(this)
    }

    fun getDateView(): TextView {
        return txtDate
    }

    fun getNameView(): TextView {
        return txtName
    }

    fun getAmountView(): TextView {
        return txtAmount
    }

    fun getDeleteView(): ImageButton {
        return btnDelete
    }

    fun getItemHeaderView(): LinearLayoutCompat {
        return itemHeaderView
    }

    override fun onClick(v: View?) {
        Log.d("EETracker *******", "Adapter position on click: $adapterPosition")
        when (v?.id) {
            btnDelete.id -> {
                buttonClickListener?.get()?.onDeleteClickListener(adapterPosition)
            }
            reportCardView.id -> {
                buttonClickListener?.get()?.onItemClickListener(adapterPosition)
            }
        }
    }
}
