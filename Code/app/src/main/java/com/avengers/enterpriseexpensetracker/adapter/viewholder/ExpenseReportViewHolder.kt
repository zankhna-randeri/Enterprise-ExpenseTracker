package com.avengers.enterpriseexpensetracker.adapter.viewholder

import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.adapter.ItemButtonClickListener
import java.lang.ref.WeakReference

class ExpenseReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private var txtDate = itemView.findViewById<TextView>(R.id.txt_date)
    private var txtName = itemView.findViewById<TextView>(R.id.txt_name)
    private var txtAmount = itemView.findViewById<TextView>(R.id.txt_amount)
    private var btnDelete = itemView.findViewById<ImageButton>(R.id.btnDelete)

    private var buttonClickListener: WeakReference<ItemButtonClickListener>? = null

    constructor(itemView: View, buttonClickListener: ItemButtonClickListener) : this(itemView) {
        this.buttonClickListener = WeakReference(buttonClickListener)
        btnDelete.setOnClickListener(this)
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

    override fun onClick(v: View?) {
        Log.d("EETracker *******", "Adapter position on click: $adapterPosition")
        buttonClickListener?.get()?.onPositionClickListener(adapterPosition)
    }
}