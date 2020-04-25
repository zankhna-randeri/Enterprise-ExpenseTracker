package com.avengers.enterpriseexpensetracker.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.adapter.RecyclerClickListener
import com.avengers.enterpriseexpensetracker.adapter.ReportDetailAdapter
import com.avengers.enterpriseexpensetracker.modal.Expense
import com.avengers.enterpriseexpensetracker.modal.ExpenseReport
import com.avengers.enterpriseexpensetracker.util.Constants

class ReportDetailFragment : Fragment() {
    private var expenseView: RecyclerView? = null
    private lateinit var report: ExpenseReport
    private val args: ReportDetailFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        report = args.report
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

    private fun initView(view: View) {
        expenseView = view.findViewById(R.id.expenseReportView)
        expenseView?.layoutManager = LinearLayoutManager(activity)
    }

    private fun loadReportDetails(report: ExpenseReport) {
        activity?.applicationContext?.let { context ->
            expenseView?.adapter = ReportDetailAdapter(context,
                    report.getExpenses() as List<Expense>,
                    object : RecyclerClickListener {
                        override fun onDeleteClickListener(position: Int) {
                        }

                        override fun onItemClickListener(position: Int) {
                        }

                        override fun btnViewReceiptClickListener(position: Int) {
                            val receiptUrl = report.getExpenses()?.get(position)?.getReceiptUrl()
                            Log.d(Constants.TAG, "Receipt URL: $receiptUrl")
                        }
                    })
        }
    }
}
