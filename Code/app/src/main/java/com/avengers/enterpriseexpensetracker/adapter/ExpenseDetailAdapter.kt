package com.avengers.enterpriseexpensetracker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.adapter.viewholder.ExpenseDetailViewHolder
import com.avengers.enterpriseexpensetracker.adapter.viewholder.ExpenseReportViewHolder
import com.avengers.enterpriseexpensetracker.modal.Expense

class ExpenseDetailAdapter(private var context: Context,
                           private var expenses: List<Expense>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        lateinit var viewHolder: RecyclerView.ViewHolder
        val inflater = LayoutInflater.from(parent.context)
        val reqViewHolder = inflater.inflate(R.layout.item_expense_detail, parent, false)
        viewHolder = ExpenseReportViewHolder(reqViewHolder)
        return viewHolder
    }

    override fun getItemCount(): Int {
        return expenses.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ExpenseDetailViewHolder) {
            holder.getDateView().text = expenses[position].getDate()
            holder.getNameView().text = "name"
            holder.getAmountView().text = context.resources.getString(R.string.txt_currency_dollar_amount,
                    expenses[position].getAmount())

        }
    }
}