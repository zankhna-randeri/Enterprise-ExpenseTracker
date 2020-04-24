package com.avengers.enterpriseexpensetracker.adapter

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.adapter.viewholder.ExpenseDetailViewHolder
import com.avengers.enterpriseexpensetracker.adapter.viewholder.ExpenseReportViewHolder
import com.avengers.enterpriseexpensetracker.modal.Expense
import com.avengers.enterpriseexpensetracker.util.Constants

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
            holder.getNameView().text = generateSpannableString(expenses[position])
            holder.getAmountView().text = context.resources.getString(R.string.txt_currency_dollar_amount,
                    expenses[position].getAmount())
            holder.getCategoryView().setImageResource(getCategoryResource(expenses[position]))
        }
    }

    private fun getCategoryResource(expense: Expense): Int {
        when (expense.getCategory()) {
            Constants.Companion.ExpenseType.Food.name -> {
                return R.drawable.ic_food
            }
            Constants.Companion.ExpenseType.Travel.name -> {
                return R.drawable.ic_travel
            }
            Constants.Companion.ExpenseType.Accommodation.name -> {
                return R.drawable.ic_accommodation
            }
            Constants.Companion.ExpenseType.Other.name -> {
                return R.drawable.ic_miscellaneous
            }
        }

        return R.mipmap.ic_launcher_round
    }

    private fun generateSpannableString(expense: Expense): CharSequence? {

        var category = getCategoryText(expense) + "\n"
        var business = if (!expense.getBusinessName().isNullOrEmpty()) {
            expense.getBusinessName()
        } else {
            ""
        }
        var source = category + business
        val spannableString = SpannableString(source)
        val typeface = ResourcesCompat.getFont(context, R.font.app_font)
        spannableString.setSpan(typeface?.style?.let { StyleSpan(typeface.style) },
                0,
                source.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        // make category bold
        spannableString.setSpan(StyleSpan(Typeface.BOLD),
                0,
                category.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        //make category font bigger
        spannableString.setSpan(RelativeSizeSpan(2f), 0, category.length, 0)
        return spannableString
    }

    private fun getCategoryText(expense: Expense): String {
        var category = ""
        when (expense.getCategory()) {
            Constants.Companion.ExpenseType.Food.name -> {
                expense.getSubCategory()?.let {
                    category = expense.getSubCategory().toString()
                }
                category += ", " + Constants.Companion.ExpenseType.Food.name
            }
        }

        return category
    }
}
