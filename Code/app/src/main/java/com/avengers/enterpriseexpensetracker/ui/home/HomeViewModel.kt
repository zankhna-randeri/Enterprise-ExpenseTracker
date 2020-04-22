package com.avengers.enterpriseexpensetracker.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.avengers.enterpriseexpensetracker.modal.ExpenseReport
import com.avengers.enterpriseexpensetracker.modal.response.ApiResponse
import com.avengers.enterpriseexpensetracker.service.ExpenseTrackerWebService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {

    private var pendingExpenses = MutableLiveData<MutableList<ExpenseReport>>()
    private val webservice = ExpenseTrackerWebService.retrofit.create(ExpenseTrackerWebService::class.java)
    private var apiCallFailed = MutableLiveData<Boolean>()

    init {
        pendingExpenses.value = ArrayList()
        apiCallFailed.value = false
    }

    fun deletePendingReport(id: String, position: Int) {
        val call = webservice.deleteReport(id)
        call.enqueue(object : Callback<ApiResponse> {
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                apiCallFailed.value = true
            }

            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                response.body()?.let {
                    if (it.getStatus()) {
                        pendingExpenses.value?.removeAt(position)
                    } else {
                        apiCallFailed.value = true
                    }
                }
            }
        })
    }

    fun getPendingExpense(): MutableLiveData<MutableList<ExpenseReport>>? {
        return pendingExpenses
    }
}