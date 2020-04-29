package com.avengers.enterpriseexpensetracker.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.avengers.enterpriseexpensetracker.modal.ExpenseReport
import com.avengers.enterpriseexpensetracker.modal.Notification
import com.avengers.enterpriseexpensetracker.modal.response.ApiResponse
import com.avengers.enterpriseexpensetracker.service.EETrackerWebService
import com.avengers.enterpriseexpensetracker.util.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationViewModel : ViewModel() {

    private val webservice = EETrackerWebService.retrofit.create(EETrackerWebService::class.java)
    private var notifications = MutableLiveData<MutableList<Notification>>()
    private var deleteApiResponse = MutableLiveData<ApiResponse>()
    private var expenseReport = MutableLiveData<ExpenseReport>()

    fun getAllNotifications(emailId: String) {
        val call = webservice.getNotifications(emailId)
        call.enqueue(object : Callback<MutableList<Notification>> {

            override fun onFailure(call: Call<MutableList<Notification>>, t: Throwable) {
                Log.e(Constants.TAG, "API Failed getNotifications")
                notifications.postValue(null)
            }

            override fun onResponse(call: Call<MutableList<Notification>>,
                                    response: Response<MutableList<Notification>>) {
                response.body()?.let {
                    Log.d(Constants.TAG, "API Response getNotifications: ${response.body().toString()}")
                    notifications.postValue(it)
                }
            }
        })
    }

    fun getNotifications(): MutableLiveData<MutableList<Notification>> {
        return notifications
    }

    fun deleteNotification(position: Int) {
        val notificationId = notifications.value?.get(position)?.getId()
        notificationId?.let { id ->
            val call = webservice.deleteNotification(id)
            call.enqueue(object : Callback<ApiResponse> {
                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    deleteApiResponse.postValue(ApiResponse(false))
                }

                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    response.body()?.let {
                        Log.d(Constants.TAG, "API Response deleteNotification: ${response.body().toString()}")
                        if (it.isSuccess()) {
                            notifications.value?.removeAt(position)
                            notifications.postValue(notifications.value)
                        }
                        deleteApiResponse.postValue(it)
                    }
                }
            })
        }
    }

    fun getDeleteResponse(): MutableLiveData<ApiResponse> {
        return deleteApiResponse
    }

    fun getExpenseReport(position: Int) {
        val reportId = notifications.value?.get(position)?.getReportId()
        reportId?.let { id ->
            val call = webservice.getReportDetailById(id)
            call.enqueue(object : Callback<ExpenseReport?> {
                override fun onFailure(call: Call<ExpenseReport?>, t: Throwable) {
                    Log.e(Constants.TAG, "API Failed getReportDetailById")
                    expenseReport.postValue(null)
                }

                override fun onResponse(call: Call<ExpenseReport?>, response: Response<ExpenseReport?>) {
                    response.body()?.let {
                        Log.d(Constants.TAG,
                                "API Response getReportDetailById: ${response.body().toString()}")
                        expenseReport.postValue(it)
                    }
                }
            })
        }
    }

    fun getExpenseReport(): MutableLiveData<ExpenseReport> {
        return expenseReport
    }

    fun clearExpenseReport() {
        expenseReport.postValue(null)
    }
}