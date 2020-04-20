package com.avengers.enterpriseexpensetracker.ui.home

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
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.adapter.HomeViewExpenseAdapter
import com.avengers.enterpriseexpensetracker.modal.ExpenseReport
import com.avengers.enterpriseexpensetracker.modal.response.ApiResponse
import com.avengers.enterpriseexpensetracker.modal.response.CategoryWiseTotalResponse
import com.avengers.enterpriseexpensetracker.modal.response.HomeFragmentResponse
import com.avengers.enterpriseexpensetracker.modal.tracking.TrackScreenData
import com.avengers.enterpriseexpensetracker.receiver.ApiResponseReceiver
import com.avengers.enterpriseexpensetracker.service.EETrackerJobService
import com.avengers.enterpriseexpensetracker.util.AnalyticsHelper
import com.avengers.enterpriseexpensetracker.util.Constants
import com.avengers.enterpriseexpensetracker.util.Utility

class HomeFragment : Fragment() {
    private lateinit var homeViewModel: HomeViewModel
    private var approvedExpenseView: RecyclerView? = null
    private var homeScreenResponse: HomeFragmentResponse? = null
    private var categoryWiseTotalResponse: CategoryWiseTotalResponse? = null
    private var allExpenseReports: MutableList<ExpenseReport>? = null
    private var approvedExpenseReports: MutableList<ExpenseReport>? = null
    private var homeScreenResponseReceiver: BroadcastReceiver? = null

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        return inflater.inflate(R.layout.fragment_home, container, false)
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
        homeScreenResponseReceiver?.let { receiver ->
            val intentFilter = IntentFilter(Constants.BROADCAST_HOME_DATA_RESPONSE)
            activity?.applicationContext?.let { context ->
                LocalBroadcastManager.getInstance(context).registerReceiver(receiver, intentFilter)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        homeScreenResponseReceiver?.let { receiver ->
            activity?.applicationContext?.let { context ->
                LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
            }
        }
    }

    private fun initView(view: View) {
        approvedExpenseView = view.findViewById(R.id.approvedExpenseView)
        approvedExpenseView?.layoutManager = LinearLayoutManager(activity)

        fetchHomeScreenData()
    }

    private fun intiBroadcastReceiver() {
        homeScreenResponseReceiver = object : ApiResponseReceiver() {
            override fun onSuccess(context: Context?, response: ApiResponse) {
                homeScreenResponse = response as HomeFragmentResponse
                val categoryWiseTotal = homeScreenResponse?.categoryWiseExpense
                val allExpenseReports = homeScreenResponse?.expenseReports
                val approvedExpenses = fetchApprovedExpenses(allExpenseReports)

                // only check approved expenses. If there is no approved expenses,
                // means `categoryWiseTotal` must be 0.0 for all
                if (approvedExpenses.isNullOrEmpty()) {
                    // TODO: Show empty view
                    // showEmptyView()
                } else {
                    bindExpenseView(categoryWiseTotal!!, approvedExpenses)
                }
            }

            override fun onFailure(context: Context?, message: String?) {
                context?.let { Utility.getInstance().showMsg(it, message) }
            }

            override fun onReceive(context: Context?, intent: Intent?) {
                val homeFragmentResponse =
                    intent?.getParcelableExtra<HomeFragmentResponse>(Constants.EXTRA_API_RESPONSE)

                var categoryStatus = false
                homeFragmentResponse?.let { response ->
                    Log.d("EETracker ***", "response $response")
                    response.expenseReports?.let { allReports ->
                        categoryStatus = response.categoryWiseExpense?.getApiResponseStatus() ?: false
                        if (categoryStatus && !allReports.isNullOrEmpty()) {
                            onSuccess(context, response)
                        } else {
                            onFailure(context, context?.getString(R.string.txt_api_failed))
                        }
                    } ?: run {
                        onFailure(context, context?.getString(R.string.txt_api_failed))
                    }
                }
            }
        }
    }

    private fun fetchApprovedExpenses(allExpenseReports: List<ExpenseReport>?): List<ExpenseReport> {
        val approvedExpenses = ArrayList<ExpenseReport>()
        val iterator = allExpenseReports?.iterator()
        iterator?.forEach { expenseReport ->
            if (expenseReport.getReportStatus().equals(Constants.Companion.Status.Approved.name, false)) {
                approvedExpenses.add(expenseReport)
            }
        }

        return approvedExpenses
    }

    private fun bindExpenseView(categoryWiseTotal: CategoryWiseTotalResponse,
                                approvedExpenses: List<ExpenseReport>) {
        val adapter =
            activity?.applicationContext?.let {
                HomeViewExpenseAdapter(it,
                        categoryWiseTotal,
                        approvedExpenses)
            }
        approvedExpenseView?.adapter = adapter
    }

    private fun fetchHomeScreenData() {
        val intent = Intent(activity?.applicationContext, EETrackerJobService::class.java).apply {
            action = Constants.ACTION_FETCH_HOME_DATA
        }
        Utility.getInstance().startExpenseTrackerService(context, intent)
    }
}