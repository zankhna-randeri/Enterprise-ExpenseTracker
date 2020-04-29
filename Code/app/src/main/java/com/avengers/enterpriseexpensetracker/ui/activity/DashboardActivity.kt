package com.avengers.enterpriseexpensetracker.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.modal.response.ApiResponse
import com.avengers.enterpriseexpensetracker.receiver.ApiResponseReceiver
import com.avengers.enterpriseexpensetracker.service.EETrackerJobService
import com.avengers.enterpriseexpensetracker.util.Constants
import com.avengers.enterpriseexpensetracker.util.EETrackerPreferenceManager
import com.avengers.enterpriseexpensetracker.util.Utility
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.iid.FirebaseInstanceId

class DashboardActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var updateTokenResponseReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        FirebaseApp.initializeApp(applicationContext)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        FirebaseCrashlytics.getInstance().sendUnsentReports()

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val navInflater = navController.navInflater
        val navGraph = navInflater.inflate(R.navigation.app_navigation_graph)
        navGraph.startDestination = getStartDestination(intent)
        navController.graph = navGraph

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home,
                R.id.nav_add_expense,
                R.id.nav_report_history,
                R.id.nav_change_pwd,
                R.id.nav_logout), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val header = navigationView.getHeaderView(0)
        val txtName = header.findViewById<TextView>(R.id.navHeader)
        val txtEmail = header.findViewById<TextView>(R.id.navSubHeader)
        txtName.text = EETrackerPreferenceManager.getUserFullName(this)
        txtEmail.text = EETrackerPreferenceManager.getUserEmail(this)

        initBroadcastReceiver()
        updateDeviceTokenIfRequired()
    }

    private fun getStartDestination(intent: Intent): Int {
        return if (intent.getBooleanExtra(Constants.EXTRA_SHOULD_START_NOTIFICATION, false)) {
            R.id.nav_notification
        } else {
            R.id.nav_home
        }
    }

    override fun onPause() {
        super.onPause()
        updateTokenResponseReceiver?.let {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(it)
        }
    }

    override fun onResume() {
        super.onResume()
        updateTokenResponseReceiver?.let {
            val intentFilter = IntentFilter(Constants.BROADCAST_UPDATE_DEVICE_TOKEN)
            LocalBroadcastManager.getInstance(this)
                    .registerReceiver(it, intentFilter)
        }
    }

    private fun initBroadcastReceiver() {
        updateTokenResponseReceiver = object : ApiResponseReceiver() {
            var appDataBundle: Bundle? = null

            override fun onSuccess(context: Context?, response: ApiResponse) {
                appDataBundle?.let { bundle ->
                    val deviceToken = bundle.getString(Constants.EXTRA_DEVICE_TOKEN)
                    deviceToken?.let { token ->
                        EETrackerPreferenceManager.saveDeviceToken(context, token)
                        Log.d(Constants.TAG, "FCM token updated to prefs, Token : $token")
                    }
                }
            }

            override fun onFailure(context: Context?, message: String?) {
                Log.e(Constants.TAG, "Could not update device token on server")
                //TODO: Log this on firebase analytics
            }

            override fun onReceive(context: Context?, intent: Intent?) {
                val response = intent?.getParcelableExtra<ApiResponse>(Constants.EXTRA_API_RESPONSE)
                appDataBundle = intent?.getBundleExtra(Constants.EXTRA_APP_DATA_BUNDLE)

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
    }

    private fun updateDeviceTokenIfRequired() {
        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(Constants.TAG, "getInstanceId failed", task.exception)
                        return@OnCompleteListener
                    }

                    // Get new Instance ID token
                    val deviceToken = task.result?.token

                    // Log and toast
                    Log.d(Constants.TAG, "FCM Token : $deviceToken")

                    val savedToken = EETrackerPreferenceManager.getDeviceToken(this)
                    if (!deviceToken.isNullOrBlank() && (savedToken.isNullOrBlank() || savedToken != deviceToken)) {
                        updateTokenOnServer(deviceToken)
                    }
                })
    }

    private fun updateTokenOnServer(token: String) {
        val intent = Intent(applicationContext, EETrackerJobService::class.java).apply {
            action = Constants.ACTION_UPDATE_DEVICE_TOKEN
            putExtra(Constants.EXTRA_DEVICE_TOKEN, token)
        }
        Utility.getInstance().startExpenseTrackerService(applicationContext, intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.dashboard, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
