package com.avengers.enterpriseexpensetracker.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.adapter.HomeViewExpenseAdapter
import com.avengers.enterpriseexpensetracker.adapter.RecyclerClickListener
import com.avengers.enterpriseexpensetracker.modal.ExpenseReport
import com.avengers.enterpriseexpensetracker.modal.response.CategoryWiseTotalResponse
import com.avengers.enterpriseexpensetracker.modal.tracking.TrackScreenData
import com.avengers.enterpriseexpensetracker.util.AnalyticsHelper
import com.avengers.enterpriseexpensetracker.util.EETrackerPreferenceManager
import com.avengers.enterpriseexpensetracker.util.NetworkHelper
import com.avengers.enterpriseexpensetracker.util.Utility
import com.avengers.enterpriseexpensetracker.viewmodel.HomeViewModel

class HomeFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var homeViewModel: HomeViewModel
    private var pendingExpenseView: RecyclerView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var progress: LinearLayout? = null
    private var txtProgressMsg: TextView? = null
    private var adapter: HomeViewExpenseAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

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
        fetchHomeScreenData()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_notification -> {
                view?.findNavController()?.navigate(HomeFragmentDirections.actionNavHomeToNotificationFragment())
            }
        }

        return false
    }

    override fun onResume() {
        super.onResume()
        Log.d("EETracker *******", "Invoked onResume()")
        initObserver()
        homeViewModel.addMergeDataSources()
    }

    override fun onPause() {
        super.onPause()
        Log.d("EETracker *******", "Invoked onPause()")
        homeViewModel.removeMergeDataSources()
    }

    override fun onRefresh() {
        fetchHomeScreenData()
    }

    private fun initView(view: View) {
        pendingExpenseView = view.findViewById(R.id.expenseReportView)
        pendingExpenseView?.layoutManager = LinearLayoutManager(activity)
        progress = view.findViewById(R.id.lyt_progress)
        txtProgressMsg = progress?.findViewById(R.id.txt_progress_msg)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshHome)
        swipeRefreshLayout?.setOnRefreshListener(this)
        swipeRefreshLayout?.setColorSchemeResources(R.color.colorPrimary,
                R.color.color_chart_1,
                R.color.color_chart_3)
    }

    private fun initObserver() {
        homeViewModel.getHomeScreenData().observe(viewLifecycleOwner, Observer { homeMergedData ->
            Log.d("EETracker *******", "Invoked observer on getHomeScreenData()")
            if (homeMergedData.categoryWiseExpense != null && homeMergedData.expenseReports != null) {
                swipeRefreshLayout?.isRefreshing = false
                bindExpenseView(homeMergedData.categoryWiseExpense!!, homeMergedData.expenseReports!!)
            }
        })
        homeViewModel.getPendingExpenses()
                ?.observe(viewLifecycleOwner,
                        Observer {
                            Log.d("EETracker *******", "Invoked observer on getPendingExpenses()")
                            hideLoadingView()
                            adapter?.notifyDataSetChanged()
                        })
        homeViewModel.getApiCallStatus()?.observe(viewLifecycleOwner, Observer { failed ->
            Log.d("EETracker *******", "Invoked observer on getApiCallStatus()")
            if (failed) {
                activity?.applicationContext?.let {
                    Utility.getInstance().showMsg(it, it.getString(R.string.txt_api_failed))
                }
            }
        })
    }

    private fun bindExpenseView(categoryWiseTotal: CategoryWiseTotalResponse,
                                pendingExpenses: MutableList<ExpenseReport>) {
        adapter =
            activity?.applicationContext?.let {
                HomeViewExpenseAdapter(it,
                        categoryWiseTotal,
                        pendingExpenses,
                        object : RecyclerClickListener {
                            override fun onDeleteClickListener(position: Int) {
                                confirmDelete(position)
                            }

                            override fun onItemClickListener(position: Int) {
                                val report = pendingExpenses[position - 1]
                                val action =
                                    HomeFragmentDirections.actionNavHomeToNavReportDetail(
                                            report)
                                view?.findNavController()?.navigate(action)
                            }

                            override fun btnViewReceiptClickListener(position: Int) {
                            }
                        })
            }
        pendingExpenseView?.adapter = adapter
    }

    private fun fetchHomeScreenData() {
        swipeRefreshLayout?.isRefreshing = true

        EETrackerPreferenceManager.getUserEmail(activity?.applicationContext)?.let {
            homeViewModel.fetchHomeScreenData(it)
        }
    }

    private fun confirmDelete(position: Int) {
        val builder = context?.let { AlertDialog.Builder(ContextThemeWrapper(it, R.style.AlertDialogTheme)) }
        builder?.setTitle(getString(R.string.txt_confirm_dialog_title))
        builder?.setMessage(getString(R.string.txt_confirm_delete))
        builder?.setPositiveButton(getString(R.string.txt_yes)) { dialog, which ->
            dialog.dismiss()
            activity?.applicationContext?.let { context ->
                if (NetworkHelper.hasNetworkAccess(context)) {
                    // As there is headerview in Adapter, expense delete position will be actual position - 1
                    showLoadingView()
                    homeViewModel.deletePendingReport(position - 1)
                }
            }
        }
        builder?.setNegativeButton(getString(R.string.txt_no)) { dialog, which ->
            dialog.dismiss()
        }

        builder?.show()
    }

    private fun showLoadingView() {
        progress?.visibility = View.VISIBLE
        txtProgressMsg?.text = getString(R.string.txt_delete_progress)
    }

    private fun hideLoadingView() {
        progress?.visibility = View.GONE
    }
}
