package com.avengers.enterpriseexpensetracker.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.avengers.enterpriseexpensetracker.modal.request.ChangePasswordRequest
import com.avengers.enterpriseexpensetracker.modal.response.ApiResponse
import com.avengers.enterpriseexpensetracker.service.EETrackerWebService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordViewModel : ViewModel() {

    private val webservice = EETrackerWebService.retrofit.create(EETrackerWebService::class.java)
    private var apiCallStatus = MutableLiveData<Boolean>()

    fun changePassword(emailId: String, oldPassword: String, newPassword: String) {
        val changePasswordReq = ChangePasswordRequest(emailId, oldPassword, newPassword)
        val call = webservice.changePassword(changePasswordReq)
        call.enqueue(object : Callback<ApiResponse> {
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                apiCallStatus.postValue(false)
            }

            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                response.body()?.let {
                    apiCallStatus.postValue(it.getStatus())
                }
            }
        })
    }

    fun getApiCallStatus(): MutableLiveData<Boolean> {
        return apiCallStatus
    }
}