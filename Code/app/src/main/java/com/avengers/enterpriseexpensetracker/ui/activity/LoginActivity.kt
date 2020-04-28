package com.avengers.enterpriseexpensetracker.ui.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.modal.request.LoginUser
import com.avengers.enterpriseexpensetracker.modal.response.ApiResponse
import com.avengers.enterpriseexpensetracker.modal.response.LoginResponse
import com.avengers.enterpriseexpensetracker.modal.tracking.TrackLoginData
import com.avengers.enterpriseexpensetracker.modal.tracking.TrackScreenData
import com.avengers.enterpriseexpensetracker.receiver.ApiResponseReceiver
import com.avengers.enterpriseexpensetracker.service.EETrackerJobService
import com.avengers.enterpriseexpensetracker.util.AnalyticsHelper
import com.avengers.enterpriseexpensetracker.util.Constants
import com.avengers.enterpriseexpensetracker.util.EETrackerPreferenceManager
import com.avengers.enterpriseexpensetracker.util.Utility
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var activityLayout: CoordinatorLayout
    private lateinit var forgotPwd: Button
    private var toolbar: Toolbar? = null
    private var title: TextView? = null
    private var inputEmail: TextInputLayout? = null
    private var inputPassword: TextInputLayout? = null
    private var progress: LinearLayout? = null
    private var txtProgressMsg: TextView? = null
    private var btnSubmit: Button? = null
    private var loginResponseReceiver: BroadcastReceiver? = null

    companion object {
        private const val REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(applicationContext)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        FirebaseCrashlytics.getInstance().sendUnsentReports();

        if (isAlreadyLoggedIn()) {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            this.finish()
        } else {
            initView()
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_CODE)
            }
        }
    }

    private fun isAlreadyLoggedIn(): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        return preferences.getBoolean(Constants.PREFS_LOGIN, false)
    }

    private fun initView() {
        activityLayout = findViewById(R.id.lyt_login)
        toolbar = findViewById(R.id.toolbar)
        title = findViewById(R.id.toolbar_title)
        inputEmail = findViewById(R.id.txt_input_email)
        inputPassword = findViewById(R.id.txt_input_password)
        btnSubmit = findViewById(R.id.btn_login_submit)
        btnSubmit?.setOnClickListener(this)
        progress = findViewById(R.id.lyt_progress)
        txtProgressMsg = progress?.findViewById(R.id.txt_progress_msg)

        // Setup forgot password link
        forgotPwd = findViewById(R.id.btn_forgot_pwd)
        forgotPwd.paintFlags = forgotPwd.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        setUpToolbar()

        initBroadcast()

        AnalyticsHelper.getInstance().trackViewScreenEvent(this, TrackScreenData("Login"))
    }

    private fun initBroadcast() {
        loginResponseReceiver = object : ApiResponseReceiver() {
            override fun onSuccess(context: Context?, response: ApiResponse) {
                val res = response as LoginResponse
                EETrackerPreferenceManager.saveLoginPrefs(res.getEmail()?.trim(),
                        res.getFname(),
                        res.getLname(),
                        context)
                val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }

            override fun onFailure(context: Context?, message: String?) {
                val snackbar = Snackbar.make(activityLayout, message.toString(),
                        Snackbar.LENGTH_LONG)
                context?.let {
                    snackbar.view.setBackgroundColor(ContextCompat.getColor(it,
                            android.R.color.holo_red_light))
                }
                snackbar.show()
            }

            override fun onReceive(context: Context?, intent: Intent?) {
                val response = intent?.getParcelableExtra<LoginResponse>(Constants.EXTRA_API_RESPONSE)
                response?.let { res ->
                    val statusSuccess = res.isApiResponseSuccess() ?: false
                    if (statusSuccess) {
                        onSuccess(context, res)
                    } else {
                        Log.d("EETracker *******", "Failed Login message: ${res.getResponseMessage()}")
                        onFailure(context,
                                res.getResponseMessage()
                                    ?: context?.resources?.getString(R.string.failed_login))
                    }
                }
                hideLoadingView()
            }
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        title?.text = getString(R.string.app_name)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return false
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            btnSubmit?.id -> {
                AnalyticsHelper.getInstance().trackLogin(this, TrackLoginData("email"))
                val email = inputEmail?.editText?.text.toString()
                val password = inputPassword?.editText?.text.toString()
                handleLogin(email, password)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loginResponseReceiver?.let {
            val intentFilter = IntentFilter(Constants.BROADCAST_LOGIN_RESPONSE)
            LocalBroadcastManager.getInstance(this).registerReceiver(loginResponseReceiver!!, intentFilter)
        }
    }

    override fun onPause() {
        super.onPause()
        loginResponseReceiver?.let {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(loginResponseReceiver!!)
        }
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }

    private fun handleLogin(email: String?, password: String?) {
        try {
            Utility.getInstance().hideKeyboard(this)

            if (password.isNullOrBlank() || !isValidEmail(email)) {
                Utility.getInstance().showMsg(applicationContext,
                        getString(R.string.enter_login_info))
            } else {
                showLoadingView()
                val user = LoginUser(
                        email!!.trim(),
                        password)
                val intent = Intent(this, EETrackerJobService::class.java).apply {
                    putExtra(Constants.EXTRA_LOGIN_USER, user)
                    action = Constants.ACTION_LOGIN
                }
                Utility.getInstance().startExpenseTrackerService(this, intent)
            }
        } catch (e: Exception) {
            Log.e("EETracker *******", "Exception in handleLogin ${e.message}")
            e.printStackTrace()
            hideLoadingView()
        }
    }

    fun isValidEmail(email: String?): Boolean {
        return (!email.isNullOrBlank()) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showLoadingView() {
        progress?.visibility = View.VISIBLE
        txtProgressMsg?.text = getString(R.string.txt_login_progress)
    }

    private fun hideLoadingView() {
        progress?.visibility = View.GONE
    }
}
