package com.avengers.enterpriseexpensetracker.ui.fragment

import android.app.DatePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.adapter.AllExpenseReportAdapter
import com.avengers.enterpriseexpensetracker.adapter.RecyclerClickListener
import com.avengers.enterpriseexpensetracker.modal.ExpenseReport
import com.avengers.enterpriseexpensetracker.modal.response.ApiResponse
import com.avengers.enterpriseexpensetracker.modal.response.GetAllReportsResponse
import com.avengers.enterpriseexpensetracker.modal.tracking.TrackScreenData
import com.avengers.enterpriseexpensetracker.receiver.ApiResponseReceiver
import com.avengers.enterpriseexpensetracker.service.EETrackerJobService
import com.avengers.enterpriseexpensetracker.util.AnalyticsHelper
import com.avengers.enterpriseexpensetracker.util.Constants
import com.avengers.enterpriseexpensetracker.util.EETrackerDateFormatManager
import com.avengers.enterpriseexpensetracker.util.Utility
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AllReportsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private var allExpenseView: RecyclerView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private lateinit var fromDateInput: TextInputLayout
    private lateinit var toDateInput: TextInputLayout
    private lateinit var btnFilterDate: Button
    private var fetchAllReportsResponseReceiver: BroadcastReceiver? = null
    private var filterReportsByDateReceiver: BroadcastReceiver? = null
    private var fromDateListener: DatePickerDialog.OnDateSetListener? = null
    private var toDateListener: DatePickerDialog.OnDateSetListener? = null
    private val calendar = Calendar.getInstance(Locale.US)
    private var fromDate: String = ""
    private var toDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_all_reports, container, false)
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

        filterReportsByDateReceiver?.let { receiver ->
            val intentFilter = IntentFilter(Constants.BROADCAST_FILTER_REPORTS_BY_DATE)
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

        filterReportsByDateReceiver?.let { receiver ->
            activity?.applicationContext?.let { context ->
                LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
            }
        }
    }

    override fun onRefresh() {
        resetDateFilter()
        fetchAllReports()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun initView(view: View) {
        btnFilterDate = view.findViewById(R.id.btn_date_filter_submit)
        allExpenseView = view.findViewById(R.id.expenseReportView)
        allExpenseView?.layoutManager = LinearLayoutManager(activity)
        fromDateInput = view.findViewById(R.id.txt_from_date)
        toDateInput = view.findViewById(R.id.txt_to_date)
        fromDateInput.editText?.inputType = InputType.TYPE_NULL
        toDateInput.editText?.inputType = InputType.TYPE_NULL
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshReports)
        swipeRefreshLayout?.setOnRefreshListener(this)
        swipeRefreshLayout?.setColorSchemeResources(R.color.colorPrimary,
                R.color.color_chart_1,
                R.color.color_chart_3)

        initListeners()
        fetchAllReports()
    }

    private fun initListeners() {
        btnFilterDate.setOnClickListener {
            fromDateInput.error = null
            toDateInput.error = null
            filterReportsByDate()
        }

        fromDateListener = DatePickerDialog.OnDateSetListener { datePickerView, year, month, day ->
            val strMonth = EETrackerDateFormatManager().mapActualMonth(month)
            val strDay = EETrackerDateFormatManager().dayFormat(day)
            fromDate = "$year-$strMonth-$strDay"
            updateDate(fromDateInput, fromDate)
        }

        toDateListener = DatePickerDialog.OnDateSetListener { datePickerView, year, month, day ->
            val strMonth = EETrackerDateFormatManager().mapActualMonth(month)
            val strDay = EETrackerDateFormatManager().dayFormat(day)
            toDate = "$year-$strMonth-$strDay"
            updateDate(toDateInput, toDate)
        }

        fromDateInput.editText?.setOnClickListener {
            context?.let {
                val day = calendar[Calendar.DAY_OF_MONTH]
                val month = calendar[Calendar.MONTH]
                val year = calendar[Calendar.YEAR]
                DatePickerDialog(it, R.style.AlertDialogTheme,
                        fromDateListener,
                        year,
                        month,
                        day).show()
            }
        }

        toDateInput.editText?.setOnClickListener {
            context?.let {
                val day = calendar[Calendar.DAY_OF_MONTH]
                val month = calendar[Calendar.MONTH]
                val year = calendar[Calendar.YEAR]
                DatePickerDialog(it, R.style.AlertDialogTheme,
                        toDateListener,
                        year,
                        month,
                        day).show()
            }
        }
    }

    private fun filterReportsByDate() {
        if (fromDate.isBlank()) {
            fromDateInput.error = getString(R.string.txt_error_empty_date)
            return
        }

        if (toDate.isBlank()) {
            toDateInput.error = getString(R.string.txt_error_empty_date)
            return
        }

        if (isInvalidDateRange(fromDate, toDate)) {
            fromDateInput.error = getString(R.string.txt_error_small_from_date)
            return
        }

        val intent = Intent(activity?.applicationContext, EETrackerJobService::class.java).apply {
            action = Constants.ACTION_FILTER_REPORTS_BY_DATE
            putExtra(Constants.EXTRA_FROM_DATE, fromDate)
            putExtra(Constants.EXTRA_TO_DATE, toDate)
        }
        Utility.getInstance().startExpenseTrackerService(context, intent)
    }

    private fun updateDate(dateInputView: TextInputLayout, date: String) {
        dateInputView.editText?.setText(date)
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
                    Log.d("EETracker *******", "response $response")
                    response.reports?.let { allReports ->
                        onSuccess(context, response)
                    } ?: run {
                        // TODO: Show empty view
                        // showEmptyView()
                    }
                } ?: run {
                    onFailure(context, getString(R.string.txt_api_failed))
                }
            }
        }

        filterReportsByDateReceiver = object : ApiResponseReceiver() {
            override fun onSuccess(context: Context?, response: ApiResponse) {
                val filteredReportsResponse = response as GetAllReportsResponse
                val approvedExpenses = fetchApprovedExpenses(filteredReportsResponse.reports)
                if (approvedExpenses.isNullOrEmpty()) {
                    //TODO: showEmptyView()
                } else {
                    bindExpenseView(approvedExpenses)
                }
            }

            override fun onFailure(context: Context?, message: String?) {
                context?.let { Utility.getInstance().showMsg(it, message) }
            }

            override fun onReceive(context: Context?, intent: Intent?) {
                val filteredReports =
                    intent?.getParcelableExtra<GetAllReportsResponse>(Constants.EXTRA_API_RESPONSE)
                filteredReports?.let { response ->
                    response.reports?.let {
                        onSuccess(context, response)
                    } ?: run {
                        // TODO: Show empty view
                        // showEmptyView()
                    }
                } ?: run {
                    onFailure(context, getString(R.string.txt_api_failed))
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

    private fun bindExpenseView(expenses: List<ExpenseReport>) {
        val adapter =
            activity?.applicationContext?.let {
                AllExpenseReportAdapter(it, expenses, object : RecyclerClickListener {
                    override fun onDeleteClickListener(position: Int) {
                        TODO("Not yet implemented")
                    }

                    override fun onItemClickListener(position: Int) {
                        val report = expenses[position]
                        val action =
                            AllReportsFragmentDirections.actionNavReportHistoryToNavReportDetail(report)
                        view?.findNavController()?.navigate(action)
                    }

                    override fun btnViewReceiptClickListener(position: Int) {
                        TODO("Not yet implemented")
                    }

                })
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

    private fun isInvalidDateRange(fromDate: String, toDate: String): Boolean {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date1 = formatter.parse(fromDate)
        val date2 = formatter.parse(toDate)
        return date1 > date2
    }

    private fun resetDateFilter() {
        fromDateInput.editText?.text?.clear()
        toDateInput.editText?.text?.clear()
        fromDateInput.editText?.clearFocus()
        toDateInput.editText?.clearFocus()
        fromDate = ""
        toDate = ""
    }
}
