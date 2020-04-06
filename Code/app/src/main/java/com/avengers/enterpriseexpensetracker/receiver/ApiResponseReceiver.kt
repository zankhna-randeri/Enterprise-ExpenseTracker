package com.avengers.enterpriseexpensetracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import com.avengers.enterpriseexpensetracker.modal.response.ApiResponse

abstract class ApiResponseReceiver : BroadcastReceiver() {
    abstract fun onSuccess(context: Context?, response:ApiResponse)
    abstract fun onFailure(context: Context?, message: String?)
}