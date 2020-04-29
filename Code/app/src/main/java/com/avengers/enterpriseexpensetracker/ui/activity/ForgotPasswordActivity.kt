package com.avengers.enterpriseexpensetracker.ui.activity

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.avengers.enterpriseexpensetracker.R
import com.google.android.material.textfield.TextInputLayout

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var viewSwitcher: ViewSwitcher
    private lateinit var btnSendOTP: Button
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
        title.text = getString(R.string.app_name)
    }
}
