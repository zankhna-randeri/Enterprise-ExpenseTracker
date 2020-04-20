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
        intent.action?.let { action ->
            when (action) {
                Constants.ACTION_LOGIN -> {
                    val user = intent.getParcelableExtra(Constants.EXTRA_LOGIN_USER) as LoginUser
                    handleActionLogin(user, action)
                }
                Constants.ACTION_RECEIPT_SCAN -> {
                    val receiptPath = intent.getStringExtra(Constants.EXTRA_UPLOAD_RECEIPT_PATH)
                    val expenseType = intent.getStringExtra(Constants.EXTRA_UPLOAD_EXPENSE_TYPE)
                    handleActionUploadReceipt(receiptPath, expenseType, action)
                }
                Constants.ACTION_SUBMIT_EXPENSE_REPORT -> {
                    val expenseReport =
                        intent.getParcelableExtra(Constants.EXTRA_SUBMIT_EXPENSE_REPORT) as ExpenseReport
                    handleActionSubmitExpenseReport(expenseReport, action)
                }
                Constants.ACTION_CATEGORY_TOTAL_EXPENSE -> {
                    handleCategoryWiseExpense(action)
                }
            }
        }
    }

    private fun handleActionLogin(user: LoginUser, action: String) {
        if (NetworkHelper.hasNetworkAccess(applicationContext)) {
            Log.d("EETracker ***", "Login request $user ")
            val call: Call<LoginResponse> = webservice.loginUser(user)
            val response = call.execute()
            handleApiResponse(response.body(), action)
        }
    }

    private fun handleActionUploadReceipt(receiptPath: String, type: String?, action: String) {
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
                handleApiResponse(response.body(), action)
            }
        }
    }

    private fun handleActionSubmitExpenseReport(expenseReport: ExpenseReport, action: String) {
        if (NetworkHelper.hasNetworkAccess(applicationContext)) {
            Log.d("EETracker ***", "API Request expenseReport: $expenseReport")
            val call = webservice.submitExpenseReport(expenseReport)
            val response = call.execute()
            Log.d("EETracker ***", "API Response expenseReport: $response")
            handleApiResponse(response.body(), action)
        }
    }

    private fun handleCategoryWiseExpense(action: String) {
        if (NetworkHelper.hasNetworkAccess(applicationContext)) {
            EETrackerPreferenceManager.getUserEmail(applicationContext)?.let {
                val call = webservice.getCategoryWiseExpenseApproved(it)
                val response = call.execute()
                handleApiResponse(response.body(), action)
            }
        }
    }

    private fun handleApiResponse(response: ApiResponse?, action: String) {
        var responseIntent: Intent? = null
        when (action) {
            Constants.ACTION_LOGIN -> {
                responseIntent = Intent(Constants.BROADCAST_LOGIN_RESPONSE)
            }
            Constants.ACTION_RECEIPT_SCAN -> {
                responseIntent = Intent(Constants.BROADCAST_RECEIPT_SCAN_RESPONSE)
            }
            Constants.ACTION_SUBMIT_EXPENSE_REPORT -> {
                responseIntent = Intent(Constants.BROADCAST_SUBMIT_EXPENSE_REPORT_RESPONSE)
            }
            Constants.ACTION_CATEGORY_TOTAL_EXPENSE -> {
                responseIntent = Intent(Constants.BROADCAST_CATEGORY_TOTAL_EXPENSE_RESPONSE)
            }
        }

        responseIntent?.let {
            it.putExtra(Constants.EXTRA_API_RESPONSE, response)
            val broadcastManager = LocalBroadcastManager.getInstance(applicationContext)
            broadcastManager.sendBroadcast(it)
        }
    }
}
