package com.avengers.enterpriseexpensetracker.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.avengers.enterpriseexpensetracker.modal.Notification
import com.avengers.enterpriseexpensetracker.service.EETrackerWebService
import com.avengers.enterpriseexpensetracker.util.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationViewModel : ViewModel() {

    private val webservice = EETrackerWebService.retrofit.create(EETrackerWebService::class.java)
    private var apiResponse = MutableLiveData<MutableList<Notification>>()

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
}