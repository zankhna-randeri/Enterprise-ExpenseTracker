package com.avengers.enterpriseexpensetracker.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.avengers.enterpriseexpensetracker.modal.LoginUser
import com.avengers.enterpriseexpensetracker.modal.response.LoginResponse
import com.avengers.enterpriseexpensetracker.util.Constants
import com.avengers.enterpriseexpensetracker.util.EETrackerPreferenceManager
import com.avengers.enterpriseexpensetracker.util.NetworkHelper
import com.avengers.enterpriseexpensetracker.util.Utility
import retrofit2.Call

private const val EXTRA_EMAIL = "enterpriseexpensetracker.service.extra.EMAIL_ID"
private const val EXTRA_PASSWORD = "enterpriseexpensetracker.service.extra.PASSWORD"

private const val JOB_ID = 1000

/**
 * An [JobIntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * helper methods.
 */
class LoginService : JobIntentService() {

    companion object {

        /**
         * Starts this service to perform work with the given parameters. If
         * the service is already performing a task this action will be queued.
         */
        fun enqueueWork(context: Context?, intent: Intent?) {
            if (context != null && intent != null) {
                enqueueWork(context, LoginService.javaClass, JOB_ID, intent)
            }
        }

        /**
         * Starts this service to perform action Baz with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        // TODO: Customize helper method
//        @JvmStatic
//        fun startActionBaz(context: Context, param1: String, param2: String) {
//            val intent = Intent(context, LoginService::class.java).apply {
//                action = ACTION_BAZ
//                putExtra(EXTRA_PARAM1, param1)
//                putExtra(EXTRA_PARAM2, param2)
//            }
//            context.startService(intent)
//        }
    }

    override fun onHandleWork(intent: Intent) {
        when (intent.action) {
            Constants.ACTION_LOGIN -> {
                val user = intent.getParcelableExtra(Constants.EXTRA_LOGIN_USER) as LoginUser
                handleActionLogin(user)
            }
        }
    }


    private fun handleActionLogin(user: LoginUser) {
        if (NetworkHelper.hasNetworkAccess(applicationContext)) {
            val webservice: ExpenseTrackerWebService =
                ExpenseTrackerWebService.retrofit.create(ExpenseTrackerWebService::class.java)
            val call: Call<LoginResponse> = webservice.loginUser(user)
            handleResponse(call.execute().body())
        }
    }

    private fun handleResponse(response: LoginResponse?) {
        if (response?.getMessage() != null) {
            var responseIntent = Intent(Constants.BROADCAST_LOGIN_RESPONSE).apply {
                putExtra(Constants.EXTRA_API_RESPONSE, response)
            }
            val broadcastManager: LocalBroadcastManager = LocalBroadcastManager
                    .getInstance(applicationContext)
            broadcastManager.sendBroadcast(responseIntent)
        }
    }
}
