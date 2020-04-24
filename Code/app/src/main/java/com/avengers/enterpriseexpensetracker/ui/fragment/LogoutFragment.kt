package com.avengers.enterpriseexpensetracker.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.avengers.enterpriseexpensetracker.ui.activity.LoginActivity
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.util.EETrackerPreferenceManager
import com.avengers.enterpriseexpensetracker.util.Utility

class LogoutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_logout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logout()
    }

    private fun logout() {
        activity?.applicationContext?.let { context ->
            EETrackerPreferenceManager.logout(context)
            Utility.getInstance().showMsg(context, getString(R.string.txt_logout_done))
        }

        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        activity?.finish()
    }
}
