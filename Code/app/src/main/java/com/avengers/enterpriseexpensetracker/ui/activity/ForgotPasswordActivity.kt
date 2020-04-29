package com.avengers.enterpriseexpensetracker.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.service.EETrackerJobService
import com.avengers.enterpriseexpensetracker.util.Constants
import com.avengers.enterpriseexpensetracker.util.Utility
import com.google.android.material.textfield.TextInputLayout

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        initView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return false
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
                    val intent = Intent(this, EETrackerJobService::class.java).apply {
                        putExtra(Constants.EXTRA_EMAIL, emailId)
                        action = Constants.ACTION_REQUEST_OTP
                    }
                    Utility.getInstance().startExpenseTrackerService(this, intent)
                } else {
                    inputEmail.error = getString(R.string.txt_error_invalid_email)
                }
            }
            btnSubmitOTP.id -> {

            }
        }
    }
}
