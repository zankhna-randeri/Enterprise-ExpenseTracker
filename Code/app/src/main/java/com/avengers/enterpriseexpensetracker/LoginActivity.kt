package com.avengers.enterpriseexpensetracker

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.avengers.enterpriseexpensetracker.modal.LoginUser
import com.avengers.enterpriseexpensetracker.modal.response.ApiResponse
import com.avengers.enterpriseexpensetracker.modal.response.LoginResponse
import com.avengers.enterpriseexpensetracker.receiver.ApiResponseReceiver
import com.avengers.enterpriseexpensetracker.service.LoginService
import com.avengers.enterpriseexpensetracker.util.Constants
import com.avengers.enterpriseexpensetracker.util.EETrackerPreferenceManager
import com.avengers.enterpriseexpensetracker.util.Utility
import com.google.android.material.textfield.TextInputLayout
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private var toolbar: Toolbar? = null
    private var title: TextView? = null
    private var inputEmail: TextInputLayout? = null
    private var inputPassword: TextInputLayout? = null
    private var progress: LinearLayout? = null
    private var txtProgressMsg: TextView? = null
    private var btnSubmit: Button? = null
    private var loginResponseReceiver: BroadcastReceiver? = null

    companion object {
        private const val TAG = "LoginActivity"
        private const val REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login);

        if (isAlreadyLoggedIn()) {
            val intent = Intent(this, VoiceBotActivity::class.java)
            startActivity(intent)
            this.finish()
        } else {
            initView();
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
        progress = findViewById(R.id.lyt_progress)
        txtProgressMsg = progress?.findViewById(R.id.txt_progress_msg)
        toolbar = findViewById(R.id.toolbar)
        title = findViewById(R.id.toolbar_title)
        inputEmail = findViewById(R.id.txt_input_email)
        inputPassword = findViewById(R.id.txt_input_password)
        btnSubmit = findViewById(R.id.btn_login_submit)
        setUpToolbar()
        btnSubmit?.setOnClickListener(this)

        initBroadcast()
    }

    private fun initBroadcast() {
        loginResponseReceiver = LoginResponseReceiver()
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
            finish();
            return true;
        }
        return false;
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_login_submit -> {
                val email = inputEmail?.editText?.text.toString()
                val password = inputPassword?.editText?.text.toString()
                handleLogin(email, password)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (loginResponseReceiver != null) {
            val intentFilter = IntentFilter(Constants.BROADCAST_LOGIN_RESPONSE)
            LocalBroadcastManager.getInstance(this).registerReceiver(loginResponseReceiver!!, intentFilter)
        }
    }

    override fun onPause() {
        super.onPause()
        if (loginResponseReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(loginResponseReceiver!!)
        }
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }

    private fun handleLogin(email: String?, password: String?) {
        if (!password.isNullOrBlank() || !isValidEmail(email)) {
            Utility.getInstance().showMsg(applicationContext,
                    getString(R.string.enter_login_info))
        } else {
            val user = LoginUser(email, password)
            val intent = Intent(this, LoginService::class.java).apply {
                putExtra(Constants.EXTRA_LOGIN_USER, user)
                action = Constants.ACTION_LOGIN
            }
            Utility.getInstance().startExpenseTrackerService(this, intent)
        }
    }

    fun isValidEmail(email: String?): Boolean {
        return (!email.isNullOrBlank()) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private class LoginResponseReceiver : ApiResponseReceiver() {

        override fun onSuccess(context: Context?, response: ApiResponse) {
            val res = response as LoginResponse
            EETrackerPreferenceManager.saveLoginPrefs(res.getEmail(),
                    res.getFname(),
                    res.getLname(),
                    context)
            val intent = Intent(context, VoiceBotActivity::class.java)
            context?.startActivity(intent)
            (context as LoginActivity).finish()
        }

        override fun onFailure(context: Context?, message: String?) {
            Utility.getInstance().showMsg(context, message)
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            val response = intent?.getParcelableExtra<LoginResponse>(Constants.EXTRA_API_RESPONSE)
            if (response?.getApiResponseCode() != Constants.RESPONSE_OK) {
                onFailure(context, response?.getMessage())
            } else {
                onSuccess(context, response)
            }
        }
    }
}
