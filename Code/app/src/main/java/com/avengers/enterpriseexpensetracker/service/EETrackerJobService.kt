package com.avengers.enterpriseexpensetracker.service

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.avengers.enterpriseexpensetracker.modal.ExpenseReport
import com.avengers.enterpriseexpensetracker.modal.LoginUser
import com.avengers.enterpriseexpensetracker.modal.response.ApiResponse
import com.avengers.enterpriseexpensetracker.modal.response.LoginResponse
import com.avengers.enterpriseexpensetracker.modal.response.ReceiptScanResponse
import com.avengers.enterpriseexpensetracker.util.Constants
import com.avengers.enterpriseexpensetracker.util.EETrackerPreferenceManager
import com.avengers.enterpriseexpensetracker.util.NetworkHelper
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import java.io.File

/**
 * An [JobIntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * helper methods.
 */
class EETrackerJobService : JobIntentService() {
    companion object {

        private const val JOB_ID = 1000

        /**
         * Starts this service to perform work with the given parameters. If
         * the service is already performing a task this action will be queued.
         */
        @JvmStatic
        fun enqueueWork(context: Context?, intent: Intent?) {
            if (context != null && intent != null) {
                enqueueWork(context, EETrackerJobService::class.java, JOB_ID, intent)
            }
        }

        val webservice: ExpenseTrackerWebService =
            ExpenseTrackerWebService.retrofit.create(ExpenseTrackerWebService::class.java)
    }

    override fun onHandleWork(intent: Intent) {
        when (intent.action) {
            Constants.ACTION_LOGIN -> {
                val user = intent.getParcelableExtra(Constants.EXTRA_LOGIN_USER) as LoginUser
                handleActionLogin(user)
            }
            Constants.ACTION_UPLOAD -> {
                val receiptPath = intent.getStringExtra(Constants.EXTRA_UPLOAD_RECEIPT_PATH)
                val expenseType = intent.getStringExtra(Constants.EXTRA_UPLOAD_EXPENSE_TYPE)
                handleActionUploadReceipt(receiptPath, expenseType)
            }
            Constants.ACTION_SUBMIT_EXPENSE_REPORT -> {
                val expenseReport =
                    intent.getParcelableExtra(Constants.EXTRA_SUBMIT_EXPENSE_REPORT) as ExpenseReport
                handleActionSubmitExpenseReport(expenseReport)
            }
        }
    }

    private fun handleActionLogin(user: LoginUser) {
        if (NetworkHelper.hasNetworkAccess(applicationContext)) {
            Log.d("EETracker ***", "Login request $user ")
            val call: Call<LoginResponse> = webservice.loginUser(user)
            handleLoginResponse(call.execute().body())
        }
    }

    private fun handleActionUploadReceipt(receiptPath: String, type: String?) {
        if (NetworkHelper.hasNetworkAccess(applicationContext)) {
            //Create a file object using file path
            val file = File(receiptPath);
            // Create a request body with file and image media type
            val filePart = MultipartBody.Part.createFormData("file",
                    file.name, RequestBody.create(MediaType.parse("image/*"), file))

            val email = EETrackerPreferenceManager.getUserEmail(applicationContext)
                    ?.let { RequestBody.create(MediaType.parse("text/plain"), it) }
            val expenseType = type?.let {
                RequestBody.create(MediaType.parse("text/plain"), it)
            }

            Log.d("EETracker ***", "Upload request email: $email and expensetype: $expenseType ")
            if (expenseType != null && email != null) {
                val call = webservice.uploadReceipt(filePart, email, expenseType)
                val response = call.execute()
                handleReceiptScanResponse(response.body())
            }
        }
    }

    private fun handleActionSubmitExpenseReport(expenseReport: ExpenseReport) {
        if (NetworkHelper.hasNetworkAccess(applicationContext)) {
            Log.d("EETracker ***", "API Request expenseReport: $expenseReport")
            val call = webservice.submitExpenseReport(expenseReport)
            val response = call.execute()
            Log.d("EETracker ***", "API Response expenseReport: $response")
            handleSubmitExpenseResponse(response.body())
        }
    }

    private fun handleReceiptScanResponse(response: ReceiptScanResponse?) {
        val responseIntent = Intent(Constants.BROADCAST_RECEIPT_SCAN_RESPONSE).apply {
            putExtra(Constants.EXTRA_API_RESPONSE, response)
        }
        val broadcastManager = LocalBroadcastManager.getInstance(applicationContext)
        broadcastManager.sendBroadcast(responseIntent)
    }

    private fun handleLoginResponse(response: LoginResponse?) {
        val responseIntent = Intent(Constants.BROADCAST_LOGIN_RESPONSE).apply {
            putExtra(Constants.EXTRA_API_RESPONSE, response)
        }
        val broadcastManager = LocalBroadcastManager.getInstance(applicationContext)
        broadcastManager.sendBroadcast(responseIntent)
    }

    private fun handleSubmitExpenseResponse(response: ApiResponse?) {
        val responseIntent = Intent(Constants.BROADCAST_SUBMIT_EXPENSE_REPORT_RESPONSE).apply {
            putExtra(Constants.EXTRA_API_RESPONSE, response)
        }
        val broadcastManager = LocalBroadcastManager.getInstance(applicationContext)
        broadcastManager.sendBroadcast(responseIntent)
    }
}
