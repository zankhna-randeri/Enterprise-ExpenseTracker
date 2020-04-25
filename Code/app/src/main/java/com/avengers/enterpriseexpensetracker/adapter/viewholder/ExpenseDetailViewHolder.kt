package com.avengers.enterpriseexpensetracker.adapter.viewholder

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.adapter.RecyclerClickListener
import java.lang.ref.WeakReference

class ExpenseDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private var txtDate = itemView.findViewById<TextView>(R.id.txt_date)
    private var txtName = itemView.findViewById<TextView>(R.id.txt_expense_name)
    private var txtAmount = itemView.findViewById<TextView>(R.id.txt_amount)
    private var imgCategory = itemView.findViewById<ImageView>(R.id.imgCategory)
    private var btnViewReceipt = itemView.findViewById<ImageButton>(R.id.btnViewReceipt)

    private var buttonClickListener: WeakReference<RecyclerClickListener>? = null

    constructor(itemView: View, buttonClickListener: RecyclerClickListener) : this(itemView) {
        this.buttonClickListener = WeakReference(buttonClickListener)
        btnViewReceipt.setOnClickListener(this)
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

    fun getCategoryView(): ImageView {
        return imgCategory
    }

    override fun onClick(v: View?) {
        buttonClickListener?.get()?.btnViewReceiptClickListener(adapterPosition)
    }

}
