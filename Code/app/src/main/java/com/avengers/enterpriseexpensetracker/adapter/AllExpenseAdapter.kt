package com.avengers.enterpriseexpensetracker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.adapter.viewholder.ExpenseReportViewHolder
import com.avengers.enterpriseexpensetracker.modal.ExpenseReport
import com.avengers.enterpriseexpensetracker.util.Constants

class AllExpenseAdapter(private var context: Context,
                        private var expenses: List<ExpenseReport>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        lateinit var viewHolder: RecyclerView.ViewHolder
        val inflater = LayoutInflater.from(parent.context)
        val reqViewHolder = inflater.inflate(R.layout.item_expense_report, parent, false)
        viewHolder = ExpenseReportViewHolder(reqViewHolder)
        return viewHolder
    }

    override fun getItemCount(): Int {
        return expenses.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ExpenseReportViewHolder) {
            holder.getDateView().text = expenses[position].getCreationDate()
            if (expenses[position].getReportStatus()
                        .equals(Constants.Companion.Status.Rejected.name, true)) {
                holder.getItemHeaderView().setBackgroundResource(R.drawable.report_reject)
            } else {
                holder.getItemHeaderView().setBackgroundResource(R.drawable.report_approved)
            }

            holder.getNameView().text = expenses[position].getName()
            holder.getAmountView().text = context.resources.getString(R.string.txt_currency_dollar_amount,
                    expenses[position].getTotal())
        }
    }
}
