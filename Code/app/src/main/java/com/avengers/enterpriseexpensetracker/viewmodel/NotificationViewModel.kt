package com.avengers.enterpriseexpensetracker.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.avengers.enterpriseexpensetracker.modal.Notification
import com.avengers.enterpriseexpensetracker.modal.response.ApiResponse
import com.avengers.enterpriseexpensetracker.service.EETrackerWebService
import com.avengers.enterpriseexpensetracker.util.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationViewModel : ViewModel() {

    private val webservice = EETrackerWebService.retrofit.create(EETrackerWebService::class.java)
    private var apiResponse = MutableLiveData<MutableList<Notification>>()
    private var deleteApiResponse = MutableLiveData<ApiResponse>()

    fun getAllNotifications(emailId: String) {
        val call = webservice.getNotifications(emailId)
        call.enqueue(object : Callback<MutableList<Notification>> {

            override fun onFailure(call: Call<MutableList<Notification>>, t: Throwable) {
                Log.d(Constants.TAG, "API Failed getNotifications")
                apiResponse.postValue(null)
            }

            override fun onResponse(call: Call<MutableList<Notification>>,
                                    response: Response<MutableList<Notification>>) {
                response.body()?.let {
                    Log.d(Constants.TAG, "API Response getNotifications: ${response.body().toString()}")
                    apiResponse.postValue(it)
                }
            }
        })
    }

    fun getApiCallStatus(): MutableLiveData<MutableList<Notification>> {
        return apiResponse
    }

    fun deleteNotification(id: Int) {
        val call = webservice.deleteNotification(id)
        call.enqueue(object : Callback<ApiResponse> {
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                deleteApiResponse.postValue(ApiResponse(false))
            }

            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                response.body()?.let {
                    Log.d(Constants.TAG, "API Response deleteNotification: ${response.body().toString()}")
                    deleteApiResponse.postValue(it)
                }
            }
        })
    }

    fun getDeleteResponse(): MutableLiveData<ApiResponse> {
        return deleteApiResponse
    }
}