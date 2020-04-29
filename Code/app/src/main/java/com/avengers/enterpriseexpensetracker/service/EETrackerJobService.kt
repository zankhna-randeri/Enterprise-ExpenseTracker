package com.avengers.enterpriseexpensetracker.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.avengers.enterpriseexpensetracker.modal.ExpenseReport
import com.avengers.enterpriseexpensetracker.modal.request.DeviceTokenRequest
import com.avengers.enterpriseexpensetracker.modal.request.ForgotPasswordSubmitOTPRequest
import com.avengers.enterpriseexpensetracker.modal.request.LoginUser
import com.avengers.enterpriseexpensetracker.modal.response.ApiResponse
import com.avengers.enterpriseexpensetracker.modal.response.GetAllReportsResponse
import com.avengers.enterpriseexpensetracker.modal.response.LoginResponse
import com.avengers.enterpriseexpensetracker.util.Constants
import com.avengers.enterpriseexpensetracker.util.EETrackerPreferenceManager
import com.avengers.enterpriseexpensetracker.util.NetworkHelper
import com.avengers.enterpriseexpensetracker.util.Utility
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

        val webservice: EETrackerWebService =
            EETrackerWebService.retrofit.create(EETrackerWebService::class.java)
    }

    override fun onHandleWork(intent: Intent) {
        intent.action?.let { action ->
            when (action) {
                Constants.ACTION_LOGIN -> {
                    val user = intent.getParcelableExtra(Constants.EXTRA_LOGIN_USER) as LoginUser
                    handleActionLogin(user, action)
                }
                Constants.ACTION_RECEIPT_SCAN -> {
                    val receiptPath = intent.getStringExtra(Constants.EXTRA_RECEIPT_PATH)
                    val expenseType = intent.getStringExtra(Constants.EXTRA_EXPENSE_TYPE)
                    handleActionUploadReceipt(receiptPath, expenseType, action)
                }
                Constants.ACTION_SUBMIT_EXPENSE_REPORT -> {
                    val expenseReport =
                        intent.getParcelableExtra(Constants.EXTRA_EXPENSE_REPORT) as ExpenseReport
                    handleActionSubmitExpenseReport(expenseReport, action)
                }
                Constants.ACTION_FETCH_ALL_REPORTS -> {
                    handleActionAllReports(action)
                }
                Constants.ACTION_UPDATE_DEVICE_TOKEN -> {
                    val deviceToken = intent.getStringExtra(Constants.EXTRA_DEVICE_TOKEN)
                    deviceToken?.let { handleActionDeviceTokenUpdate(it, action) }
                }
                Constants.ACTION_FILTER_REPORTS_BY_DATE -> {
                    val fromDate = intent.getStringExtra(Constants.EXTRA_FROM_DATE)
                    val toDate = intent.getStringExtra(Constants.EXTRA_TO_DATE)
                    if (!fromDate.isNullOrBlank() && !toDate.isNullOrBlank()) {
                        handleActionReportFilterByDate(fromDate, toDate, action)
                    } else {
                        return
                    }
                }
                Constants.ACTION_REQUEST_OTP -> {
                    val emailId = intent.getStringExtra(Constants.EXTRA_EMAIL)
                    val otp = intent.getStringExtra(Constants.EXTRA_REQUEST_OTP)
                    if (!emailId.isNullOrBlank() && !otp.isNullOrBlank()) {
                        handleActionRequestOTP(emailId, otp, action)
                    } else {
                        return
                    }
                }
                Constants.ACTION_SUBMIT_OTP -> {
                    val emailId = intent.getStringExtra(Constants.EXTRA_EMAIL)
                    val otp = intent.getStringExtra(Constants.EXTRA_SUBMIT_OTP)
                    val password = intent.getStringExtra(Constants.EXTRA_PASSWORD)
                    if (Utility.getInstance().isAllFieldsValid(emailId, otp, password)) {
                        handleActionSubmitOTP(emailId, otp, password, action)
                    } else {
                        return
                    }
                }
                else -> {
                    Log.e(Constants.TAG, "******* No action received from intent *******")
                    return
                }
            }
        }
    }

    private fun handleActionLogin(user: LoginUser, action: String) {
        if (NetworkHelper.hasNetworkAccess(applicationContext)) {
            Log.d("EETracker *******", "Login request $user ")
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

            Log.d("EETracker *******", "Upload request email: $email and expensetype: $expenseType ")
            if (expenseType != null && email != null) {
                val call = webservice.uploadReceipt(filePart, email, expenseType)
                val response = call.execute()
                handleApiResponse(response.body(), action)
            }
        }
    }

    private fun handleActionSubmitExpenseReport(expenseReport: ExpenseReport, action: String) {
        try {
            if (NetworkHelper.hasNetworkAccess(applicationContext)) {
                Log.d("EETracker *******", "API Request expenseReport: $expenseReport")
                val call = webservice.submitExpenseReport(expenseReport)
                val response = call.execute()
                Log.d("EETracker *******", "API Response expenseReport: $response")

                // lambda to send email notification on submitting expense report
                val originalURL = "https://jr41wuzksd.execute-api.us-east-1.amazonaws.com/Test"
                val lambdaUrl = "https://cors-anywhere.herokuapp.com/$originalURL"
                val queryParams = buildQueryParams(expenseReport)
                Log.d(Constants.TAG, "Submit Report Lambda query : $queryParams")

                val emailLambda = webservice.submitReportLambda(lambdaUrl, queryParams)
                Log.d("EETracker *******", "API Request expenseReportLambda: $emailLambda")
                val lambdaRes = emailLambda.execute()

                // handle submit report api response
                handleApiResponse(response.body(), action)
            }
        } catch (e: Exception) {
            Log.e("EETracker *******", "API Request handleActionSubmitExpenseReport: ${e.message}")
        }
    }

    private fun buildQueryParams(expenseReport: ExpenseReport): String {
        var result = ""
        expenseReport.getExpenses()?.let { expenses ->
            for ((count, expense) in expenses.withIndex()) {
                result += (count.toString() + ":" +
                        EETrackerPreferenceManager.getUserEmail(applicationContext) + ","
                        + expense.getCategory() + ","
                        + "$" + expense.getAmount() + ","
                        + expense.getDate() + ","
                        + expenseReport.getName() + "--NER--")
            }
        }

        return result
    }

    private fun handleActionAllReports(action: String) {
        if (NetworkHelper.hasNetworkAccess(applicationContext)) {
            EETrackerPreferenceManager.getUserEmail(applicationContext)?.let { emailId ->
                val getAllReportCall = webservice.getAllExpenseReports(emailId)
                val getAllReportResponse = getAllReportCall.execute()
                val response = GetAllReportsResponse(getAllReportResponse.body())

                Log.d("EETracker *******", "API Response handleActionAllReports: $getAllReportResponse")
                handleApiResponse(response, action)
            }
        }
    }

    private fun handleActionDeviceTokenUpdate(deviceToken: String, action: String) {
        if (NetworkHelper.hasNetworkAccess(applicationContext)) {
            EETrackerPreferenceManager.getUserEmail(applicationContext)?.let { emailId ->
                val deviceTokenRequest = DeviceTokenRequest(emailId, deviceToken)
                val call = webservice.updateDeviceToken(deviceTokenRequest)
                val response = call.execute()

                Log.d(Constants.TAG, "API Response handleActionDeviceTokenUpdate: $response")
                val appDataBundle = Bundle()
                appDataBundle.putString(Constants.EXTRA_DEVICE_TOKEN, deviceToken)
                handleApiResponse(response.body(), action, appDataBundle)
            }
        }
    }

    private fun handleActionReportFilterByDate(fromDate: String, toDate: String, action: String) {
        if (NetworkHelper.hasNetworkAccess(applicationContext)) {
            EETrackerPreferenceManager.getUserEmail(applicationContext)
                    ?.let {
                        val call = webservice.filterReportsByDate(it, fromDate, toDate)
                        val response = call.execute()
                        val filterReportResponse = GetAllReportsResponse(response.body())
                        Log.d("EETracker *******",
                                "API Response handleActionReportFilterByDate: $filterReportResponse")
                        handleApiResponse(filterReportResponse, action)
                    }
        }
    }

    private fun handleActionRequestOTP(emailId: String, otp: String, action: String) {
        if (NetworkHelper.hasNetworkAccess(applicationContext)) {
            val call = webservice.requestOTP(emailId, otp)
            val response = call.execute()
            Log.d(Constants.TAG,
                    "API Response handleActionRequestOPT: $response")
            handleApiResponse(response.body(), action)
        }
    }

    private fun handleActionSubmitOTP(emailId: String,
                                      otp: String,
                                      password: String,
                                      action: String) {
        if (NetworkHelper.hasNetworkAccess(applicationContext)) {
            val request = ForgotPasswordSubmitOTPRequest(emailId, otp, password)
            val call = webservice.submitOTPForgotPassword(request)
            val response = call.execute()
            Log.d(Constants.TAG,
                    "API Response handleActionRequestOPT: $response")
            handleApiResponse(response.body(), action)
        }
    }

    private fun handleApiResponse(response: ApiResponse?, action: String, appDataBundle: Bundle?) {
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
            Constants.ACTION_FETCH_ALL_REPORTS -> {
                responseIntent = Intent(Constants.BROADCAST_FETCH_ALL_REPORTS)
            }
            Constants.ACTION_UPDATE_DEVICE_TOKEN -> {
                responseIntent = Intent(Constants.BROADCAST_UPDATE_DEVICE_TOKEN)
            }
            Constants.ACTION_FILTER_REPORTS_BY_DATE -> {
                responseIntent = Intent(Constants.BROADCAST_FILTER_REPORTS_BY_DATE)
            }
            Constants.ACTION_REQUEST_OTP -> {
                responseIntent = Intent(Constants.BROADCAST_REQUEST_OTP)
            }
            Constants.ACTION_SUBMIT_OTP -> {
                responseIntent = Intent(Constants.BROADCAST_SUBMIT_OTP)
            }
        }

        responseIntent?.let { intent ->
            intent.putExtra(Constants.EXTRA_API_RESPONSE, response)
            appDataBundle?.let { bundle ->
                intent.putExtra(Constants.EXTRA_APP_DATA_BUNDLE, bundle)
            }
            val broadcastManager = LocalBroadcastManager.getInstance(applicationContext)
            broadcastManager.sendBroadcast(intent)
        }
    }

    private fun handleApiResponse(response: ApiResponse?, action: String) {
        handleApiResponse(response, action, null)
    }
}
