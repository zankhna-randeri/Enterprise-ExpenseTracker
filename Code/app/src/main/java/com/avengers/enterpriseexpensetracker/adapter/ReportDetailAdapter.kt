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
import com.avengers.enterpriseexpensetracker.adapter.viewholder.CommentViewHolder
import com.avengers.enterpriseexpensetracker.adapter.viewholder.ExpenseDetailViewHolder
import com.avengers.enterpriseexpensetracker.modal.Expense
import com.avengers.enterpriseexpensetracker.util.Constants

class ReportDetailAdapter(private var context: Context,
                          private var expenses: List<Expense>,
                          private var comment: String?,
                          private var buttonClickListener: RecyclerClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_HEADER = 0
        private const val VIEW_LIST = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        lateinit var viewHolder: RecyclerView.ViewHolder
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            VIEW_HEADER -> {
                val reqViewHolder = inflater.inflate(R.layout.item_report_comment, parent, false)
                viewHolder = CommentViewHolder(reqViewHolder)
            }
            VIEW_LIST -> {
                val reqViewHolder = inflater.inflate(R.layout.item_expense_detail, parent, false)
                viewHolder = ExpenseDetailViewHolder(reqViewHolder, buttonClickListener)
            }
        }

        return viewHolder
    }

    override fun getItemCount(): Int {
        return expenses.size + 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ExpenseDetailViewHolder) {
            val actualPosition = position - 1
            holder.getDateView().text = expenses[actualPosition].getDate()
            holder.getNameView().text = generateSpannableString(expenses[actualPosition])
            holder.getAmountView().text = context.resources.getString(R.string.txt_currency_dollar_amount,
                    expenses[actualPosition].getAmount())
            holder.getCategoryView().setImageResource(getCategoryResource(expenses[actualPosition]))
        } else if (holder is CommentViewHolder) {
            if (comment.isNullOrBlank()) {
                holder.getCommentView().text = context.getString(R.string.txt_no_comment)
            } else {
                holder.getCommentView().text = context.getString(R.string.txt_comment, comment)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return VIEW_HEADER
        }

        return VIEW_LIST
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

        var category = getCategoryText(expense)
        var business = if (!expense.getBusinessName().isNullOrBlank()) {
            "\n\n" + expense.getBusinessName()
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
        spannableString.setSpan(RelativeSizeSpan(1.1f), 0, category.length, 0)
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
            Constants.Companion.ExpenseType.Travel.name -> {
                category += Constants.Companion.ExpenseType.Travel.name
            }
            Constants.Companion.ExpenseType.Accommodation.name -> {
                category += "Stay at"
            }
            Constants.Companion.ExpenseType.Other.name -> {
                category += "Other"
            }
        }

        return category
    }
}
