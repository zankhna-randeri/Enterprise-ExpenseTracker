package com.avengers.enterpriseexpensetracker.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.modal.response.ApiResponse
import com.avengers.enterpriseexpensetracker.receiver.ApiResponseReceiver
import com.avengers.enterpriseexpensetracker.service.EETrackerJobService
import com.avengers.enterpriseexpensetracker.util.Constants
import com.avengers.enterpriseexpensetracker.util.Utility
import com.google.android.material.textfield.TextInputLayout
import java.security.SecureRandom

class ForgotPasswordActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var viewSwitcher: ViewSwitcher
    private lateinit var btnRequestOTP: Button
    private lateinit var btnSubmitOTP: Button
    private lateinit var inputEmail: TextInputLayout
    private lateinit var inputOTP: TextInputLayout
    private lateinit var inputNewPassword: TextInputLayout
    private lateinit var inputConfirmNewPassword: TextInputLayout
    private lateinit var toolbar: Toolbar
    private lateinit var title: TextView
    private var requestOTPResponseReceiver: BroadcastReceiver? = null
    private var submitOTPResponseReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        initView()
        initBroadcast()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        requestOTPResponseReceiver?.let {
            val intentFilter = IntentFilter(Constants.BROADCAST_REQUEST_OTP)
            LocalBroadcastManager.getInstance(this).registerReceiver(it, intentFilter)
        }

        submitOTPResponseReceiver?.let {
            val intentFilter = IntentFilter(Constants.BROADCAST_SUBMIT_OTP)
            LocalBroadcastManager.getInstance(this).registerReceiver(it, intentFilter)
        }
    }

    override fun onPause() {
        super.onPause()
        requestOTPResponseReceiver?.let { LocalBroadcastManager.getInstance(this).unregisterReceiver(it) }
        submitOTPResponseReceiver?.let { LocalBroadcastManager.getInstance(this).unregisterReceiver(it) }
    }

    private fun initView() {
        viewSwitcher = findViewById(R.id.view_switcher_forgot_password)
        inputEmail = findViewById(R.id.txt_input_email)
        inputOTP = findViewById(R.id.txt_input_otp)
        inputNewPassword = findViewById(R.id.txt_input_new_password)
        inputConfirmNewPassword = findViewById(R.id.txt_input_confirm_new_password)
        btnRequestOTP = findViewById(R.id.btn_send_otp)
        btnRequestOTP.setOnClickListener(this)
        btnSubmitOTP = findViewById(R.id.btn_submit_otp)
        btnSubmitOTP.setOnClickListener(this)
        toolbar = findViewById(R.id.toolbar)
        title = findViewById(R.id.toolbar_title)
        setUpToolbar()
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        title.text = getString(R.string.forgot_pwd)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            btnRequestOTP.id -> {
                inputEmail.error = null
                val emailId = inputEmail.editText?.text.toString()
                if (Utility.getInstance().isValidEmail(emailId)) {
                    val otp = generateOTP(7)
                    requestOTP(emailId, otp)
                } else {
                    inputEmail.error = getString(R.string.txt_error_invalid_email)
                }
            }
            btnSubmitOTP.id -> {
                inputConfirmNewPassword.error = null
                val emailId = inputEmail.editText?.text.toString()
                val otp = inputOTP.editText?.text.toString()
                val newPassword = inputNewPassword.editText?.text.toString()
                val confirmPassword = inputConfirmNewPassword.editText?.text.toString()
                if (Utility.getInstance().isAllFieldsValid(emailId, otp, newPassword, confirmPassword)) {
                    if (newPassword == confirmPassword) {
                        submitOTP(emailId, otp, newPassword)
                    } else {
                        inputConfirmNewPassword.error = getString(R.string.txt_error_confirm_pwd)
                    }
                }
            }
        }
    }

    private fun requestOTP(emailId: String, otp: String) {
        //TODO: Show Loading view
        val intent = Intent(this, EETrackerJobService::class.java).apply {
            putExtra(Constants.EXTRA_EMAIL, emailId)
            putExtra(Constants.EXTRA_REQUEST_OTP, otp)
            action = Constants.ACTION_REQUEST_OTP
        }
        Utility.getInstance().startExpenseTrackerService(this, intent)
    }

    private fun generateOTP(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val random = SecureRandom()
        var otp = ""
        for (i in 0 until length) {
            val randomIndex = random.nextInt(chars.length)
            otp += chars[randomIndex]
        }

        return otp
    }

    private fun initBroadcast() {
        requestOTPResponseReceiver = object : ApiResponseReceiver() {
            override fun onSuccess(context: Context?, response: ApiResponse) {
                viewSwitcher.showNext()
            }

            override fun onFailure(context: Context?, message: String?) {
                context?.let { Utility.getInstance().showMsg(it, message) }
            }

            override fun onReceive(context: Context?, intent: Intent?) {
                // TODO: hideLoadingView
                val response = intent?.getParcelableExtra<ApiResponse>(Constants.EXTRA_API_RESPONSE)
                response?.let {
                    Log.d(Constants.TAG, "response $response")
                    if (response.isSuccess()) {
                        onSuccess(context, response)
                    } else {
                        onFailure(context, context?.getString(R.string.txt_api_failed))
                    }
                }
            }
        }

        submitOTPResponseReceiver = object : ApiResponseReceiver() {
            override fun onSuccess(context: Context?, response: ApiResponse) {
                finish()
                Utility.getInstance().showMsg(this@ForgotPasswordActivity, response.getMessage())
            }

            override fun onFailure(context: Context?, message: String?) {
                context?.let { Utility.getInstance().showMsg(it, message) }
            }

            override fun onReceive(context: Context?, intent: Intent?) {
                // TODO: hideLoadingView
                val response = intent?.getParcelableExtra<ApiResponse>(Constants.EXTRA_API_RESPONSE)
                response?.let {
                    Log.d(Constants.TAG, "response $response")
                    if (response.isSuccess()) {
                        onSuccess(context, response)
                    } else {
                        onFailure(context, response.getMessage())
                    }
                }
            }
        }
    }

    private fun submitOTP(emailId: String, otp: String, password: String) {
        //TODO: Show Loading view
        val intent = Intent(this, EETrackerJobService::class.java).apply {
            putExtra(Constants.EXTRA_EMAIL, emailId)
            putExtra(Constants.EXTRA_SUBMIT_OTP, otp)
            putExtra(Constants.EXTRA_PASSWORD, password)
            action = Constants.ACTION_SUBMIT_OTP
        }
        Utility.getInstance().startExpenseTrackerService(this, intent)
    }
}
