package com.avengers.enterpriseexpensetracker.ui.reports_history

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.adapter.AllExpenseAdapter
import com.avengers.enterpriseexpensetracker.modal.ExpenseReport
import com.avengers.enterpriseexpensetracker.modal.response.ApiResponse
import com.avengers.enterpriseexpensetracker.modal.response.GetAllReportsResponse
import com.avengers.enterpriseexpensetracker.modal.tracking.TrackScreenData
import com.avengers.enterpriseexpensetracker.receiver.ApiResponseReceiver
import com.avengers.enterpriseexpensetracker.service.EETrackerJobService
import com.avengers.enterpriseexpensetracker.util.AnalyticsHelper
import com.avengers.enterpriseexpensetracker.util.Constants
import com.avengers.enterpriseexpensetracker.util.Utility

class ReportsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private var allExpenseView: RecyclerView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var fetchAllReportsResponseReceiver: BroadcastReceiver? = null

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reports, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (activity != null) {
            AnalyticsHelper.getInstance().trackViewScreenEvent(activity!!, TrackScreenData("Home"))
        }

        initView(view)
        intiBroadcastReceiver()
    }

    override fun onResume() {
        super.onResume()
        fetchAllReportsResponseReceiver?.let { receiver ->
            val intentFilter = IntentFilter(Constants.BROADCAST_FETCH_ALL_REPORTS)
            activity?.applicationContext?.let { context ->
                LocalBroadcastManager.getInstance(context).registerReceiver(receiver, intentFilter)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        fetchAllReportsResponseReceiver?.let { receiver ->
            activity?.applicationContext?.let { context ->
                LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
            }
        }
    }

    override fun onRefresh() {
        fetchAllReports()
    }

    private fun initView(view: View) {
        allExpenseView = view.findViewById(R.id.expenseReportView)
        allExpenseView?.layoutManager = LinearLayoutManager(activity)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshHome)
        swipeRefreshLayout?.setOnRefreshListener(this)
        swipeRefreshLayout?.setColorSchemeResources(R.color.colorPrimary,
                R.color.color_chart_1,
                R.color.color_chart_3)

        fetchAllReports()
    }

    private fun intiBroadcastReceiver() {
        fetchAllReportsResponseReceiver = object : ApiResponseReceiver() {
            override fun onSuccess(context: Context?, response: ApiResponse) {
                val allReportsResponse = response as GetAllReportsResponse

                val approvedExpenses = fetchApprovedExpenses(allReportsResponse.reports)
                val rejectedExpenses = fetchRejectedExpenses(allReportsResponse.reports)
                var expenses: MutableList<ExpenseReport> = ArrayList()
                expenses.addAll(approvedExpenses)
                expenses.addAll(rejectedExpenses)

                if (expenses.isNullOrEmpty()) {
                    // TODO: Show empty view
                    // showEmptyView()
                } else {
                    bindExpenseView(expenses)
                }
            }

            override fun onFailure(context: Context?, message: String?) {
                context?.let { Utility.getInstance().showMsg(it, message) }
            }

            override fun onReceive(context: Context?, intent: Intent?) {
                swipeRefreshLayout?.isRefreshing = false

                val getAllReportsResponse =
                    intent?.getParcelableExtra<GetAllReportsResponse>(Constants.EXTRA_API_RESPONSE)

                getAllReportsResponse?.let { response ->
                    Log.d("EETracker ***", "response $response")
                    response.reports?.let { allReports ->
                        onSuccess(context, response)
                    } ?: run {
                        // TODO: Show empty view
                        // showEmptyView()

                    }
                }
            }
        }
    }

    private fun fetchApprovedExpenses(allExpenseReports: List<ExpenseReport>?): List<ExpenseReport> {
        val approvedExpenses = ArrayList<ExpenseReport>()
        val iterator = allExpenseReports?.iterator()
        iterator?.forEach { expenseReport ->
            if (expenseReport.getReportStatus().equals(Constants.Companion.Status.Approved.name, true)) {
                approvedExpenses.add(expenseReport)
            }
        }

        return approvedExpenses
    }

    private fun fetchRejectedExpenses(allExpenseReports: List<ExpenseReport>?): List<ExpenseReport> {
        val rejectedExpenses = ArrayList<ExpenseReport>()
        val iterator = allExpenseReports?.iterator()
        iterator?.forEach { expenseReport ->
            if (expenseReport.getReportStatus().equals(Constants.Companion.Status.Rejected.name, true)) {
                rejectedExpenses.add(expenseReport)
            }
        }

        return rejectedExpenses
    }

    private fun bindExpenseView(
        expenses: List<ExpenseReport>) {
        val adapter =
            activity?.applicationContext?.let {
                AllExpenseAdapter(it, expenses)
            }
        allExpenseView?.adapter = adapter
    }

    private fun fetchAllReports() {
        swipeRefreshLayout?.isRefreshing = true

        val intent = Intent(activity?.applicationContext, EETrackerJobService::class.java).apply {
            action = Constants.ACTION_FETCH_ALL_REPORTS
        }
        Utility.getInstance().startExpenseTrackerService(context, intent)
    }
}