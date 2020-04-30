package com.avengers.enterpriseexpensetracker.ui.fragment

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.adapter.RecyclerClickListener
import com.avengers.enterpriseexpensetracker.adapter.RecyclerViewReceiptClickListener
import com.avengers.enterpriseexpensetracker.adapter.ReportDetailAdapter
import com.avengers.enterpriseexpensetracker.modal.Expense
import com.avengers.enterpriseexpensetracker.modal.ExpenseReport
import com.avengers.enterpriseexpensetracker.util.Constants
import com.bumptech.glide.Glide

class ReportDetailFragment : Fragment() {
    private var expenseView: RecyclerView? = null
    private lateinit var report: ExpenseReport
    private val args: ReportDetailFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        report = args.report
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_report_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        loadReportDetails(report)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun initView(view: View) {
        expenseView = view.findViewById(R.id.expenseReportView)
        expenseView?.layoutManager = LinearLayoutManager(activity)
    }

    private fun loadReportDetails(report: ExpenseReport) {
        activity?.applicationContext?.let { context ->
            expenseView?.adapter = ReportDetailAdapter(context,
                    report.getExpenses() as List<Expense>, report.getReportComment(),
                    object : RecyclerViewReceiptClickListener {
                        override fun onDeleteClickListener(position: Int) {
                        }

                        override fun onItemClickListener(position: Int) {
                        }

                        override fun btnViewReceiptClickListener(position: Int) {
                            // As there is a comment view added as a header in report detail adapter,
                            // there are total expenses.size() + 1 item in list.
                            // So expense list will start from 1 rather than 0. To avoid IndexOutOfBound exception,
                            // actual position will be position - 1
                            val actualPosition = position - 1
                            val receiptUrl = report.getExpenses()?.get(actualPosition)?.getReceiptUrl()
                            Log.d(Constants.TAG, "Receipt URL: $receiptUrl")
                            receiptUrl?.let { url -> showReceiptImage(context, url) }
                        }
                    })
        }
    }

    private fun showReceiptImage(context: Context, receiptUrl: String) {
        activity?.let {
            val dialog = Dialog(it)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_view_receipt, null)
            dialog.setContentView(dialogView)

            val imageView = dialogView.findViewById<ImageView>(R.id.imgReceipt)
            Glide.with(context).load(receiptUrl).into(imageView)

            val lp = WindowManager.LayoutParams()
            lp.copyFrom(dialog.window?.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.MATCH_PARENT
            dialog.show()
            dialog.window?.attributes = lp
        }
    }
}
