package com.avengers.enterpriseexpensetracker.viewmodel

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.avengers.enterpriseexpensetracker.modal.ExpenseReport
import com.avengers.enterpriseexpensetracker.modal.response.ApiResponse
import com.avengers.enterpriseexpensetracker.modal.response.CategoryWiseTotalResponse
import com.avengers.enterpriseexpensetracker.modal.response.HomeFragmentResponse
import com.avengers.enterpriseexpensetracker.service.EETrackerWebService
import com.avengers.enterpriseexpensetracker.util.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {

    private val webservice = EETrackerWebService.retrofit.create(EETrackerWebService::class.java)
    private var apiCallFailed: MutableLiveData<Boolean>? = null

    private var homeScreenResponse = HomeFragmentResponse()
    private var pendingExpenses = MutableLiveData<MutableList<ExpenseReport>>()
    private var categoryWiseTotal = MutableLiveData<CategoryWiseTotalResponse>()
    private val liveDataMerger = MediatorLiveData<HomeFragmentResponse>()

    init {
        pendingExpenses.value = ArrayList()
        apiCallFailed = MutableLiveData()
        liveDataMerger.value = homeScreenResponse
    }

    fun addMergeDataSources() {
        // need to add source at this point rather than on constructor.
        // As, mediator live data needs to have observer binded then only onChange() is
        // invoked. At this point, observer is already bounded in the fragment.

        liveDataMerger.addSource(categoryWiseTotal) { categoryWiseTotal ->
            homeScreenResponse.categoryWiseExpense = categoryWiseTotal
            liveDataMerger.postValue(homeScreenResponse)
        }

        liveDataMerger.addSource(pendingExpenses) { pendingExpenses ->
            homeScreenResponse.expenseReports = pendingExpenses
            liveDataMerger.postValue(homeScreenResponse)
        }
    }

    fun removeMergeDataSources() {
        liveDataMerger.removeSource(categoryWiseTotal)
        liveDataMerger.removeSource(pendingExpenses)
    }

    fun getHomeScreenData(): MediatorLiveData<HomeFragmentResponse> {
        return liveDataMerger
    }

    fun fetchHomeScreenData(emailId: String) {

        // fetch CategoryWise total
        val categoryTotalCall = webservice.getCategoryWiseExpenseApproved(emailId)
        categoryTotalCall.enqueue(object : Callback<CategoryWiseTotalResponse> {
            override fun onFailure(call: Call<CategoryWiseTotalResponse>, t: Throwable) {
                apiCallFailed?.postValue(true)
            }

            override fun onResponse(call: Call<CategoryWiseTotalResponse>,
                                    response: Response<CategoryWiseTotalResponse>) {
                Log.d("EETracker *******", "API Response getCategoryWiseExpenseApproved: ${response.body()}")
                response.body()?.let {
                    if (it.getStatus()) {
                        categoryWiseTotal.postValue(it)
                    }
                    apiCallFailed?.postValue(it.getStatus())
                }
            }
        })

        // fetch All expense reports
        val getAllReportCall = webservice.getAllExpenseReports(emailId)
        getAllReportCall.enqueue(object : Callback<MutableList<ExpenseReport>> {
            override fun onFailure(call: Call<MutableList<ExpenseReport>>, t: Throwable) {
                apiCallFailed?.postValue(true)
            }

            override fun onResponse(call: Call<MutableList<ExpenseReport>>,
                                    response: Response<MutableList<ExpenseReport>>) {
                Log.d("EETracker *******", "API Response getAllExpenseReports: ${response.body()}")
                response.body()?.let {
                    pendingExpenses.postValue(fetchPendingExpenses(it))
                    apiCallFailed?.postValue(false)
                }
            }
        })
    }

    private fun fetchPendingExpenses(allExpenseReports: List<ExpenseReport>?): MutableList<ExpenseReport> {
        val pendingExpenses = ArrayList<ExpenseReport>()
        val iterator = allExpenseReports?.iterator()
        iterator?.forEach { expenseReport ->
            if (expenseReport.getReportStatus().equals(Constants.Companion.Status.Pending.name, true)) {
                pendingExpenses.add(expenseReport)
            }
        }

        return pendingExpenses
    }

    fun deletePendingReport(position: Int) {
        val reportId = pendingExpenses.value?.get(position)?.getReportId()
        reportId?.let {
            val call = webservice.deleteReport(reportId)
            call.enqueue(object : Callback<ApiResponse> {
                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    apiCallFailed?.postValue(true)
                }

                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    response.body()?.let {
                        if (it.getStatus()) {
                            pendingExpenses.value?.removeAt(position)
                            pendingExpenses.postValue(pendingExpenses.value)
                        }
                        apiCallFailed?.postValue(it.getStatus())
                    }
                }
            })
        }
    }

    fun getPendingExpenses(): MutableLiveData<MutableList<ExpenseReport>>? {
        return pendingExpenses
    }

    fun getApiCallStatus(): MutableLiveData<Boolean>? {
        return apiCallFailed
    }
}
