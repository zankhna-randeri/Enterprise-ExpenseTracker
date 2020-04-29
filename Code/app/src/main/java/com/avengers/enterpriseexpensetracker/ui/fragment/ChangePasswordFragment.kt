package com.avengers.enterpriseexpensetracker.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.util.EETrackerPreferenceManager
import com.avengers.enterpriseexpensetracker.util.NetworkHelper
import com.avengers.enterpriseexpensetracker.util.Utility
import com.avengers.enterpriseexpensetracker.viewmodel.ChangePasswordViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout

class ChangePasswordFragment : Fragment(), View.OnClickListener {

    private var viewModel: ChangePasswordViewModel? = null
    private lateinit var activityLayout: CoordinatorLayout
    private lateinit var oldPassword: TextInputLayout
    private lateinit var newPassword: TextInputLayout
    private lateinit var confirmNewPassword: TextInputLayout
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(this).get(ChangePasswordViewModel::class.java)
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initObserver()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun initView(view: View) {
        activityLayout = view.findViewById(R.id.lyt_change_password)
        oldPassword = view.findViewById(R.id.txt_input_old_password)
        newPassword = view.findViewById(R.id.txt_input_new_password)
        confirmNewPassword = view.findViewById(R.id.txt_input_confirm_new_password)
        btnSubmit = view.findViewById(R.id.btn_change_pwd_submit)
        btnSubmit.setOnClickListener(this)
    }

    private fun initObserver() {
        viewModel?.getApiCallStatus()?.observe(viewLifecycleOwner, Observer { response ->
            if (response.isSuccess()) {
                view?.findNavController()?.navigateUp()
            } else {
                activity?.applicationContext?.let { context ->
                    val snackbar = Snackbar.make(activityLayout,
                            response.getMessage() ?: getString(R.string.txt_api_failed),
                            Snackbar.LENGTH_LONG)
                    snackbar.view.setBackgroundColor(ContextCompat.getColor(context,
                            android.R.color.holo_red_light))
                    snackbar.show()
                }
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            btnSubmit.id -> {
                confirmNewPassword.error = null
                val oldPassword = oldPassword.editText?.text.toString()
                val newPassword = newPassword.editText?.text.toString()
                val confirmPassword = confirmNewPassword.editText?.text.toString()
                if (Utility.getInstance().isAllFieldsValid(oldPassword, newPassword, confirmPassword)) {
                    if ((newPassword == confirmPassword)) {
                        activity?.applicationContext?.let { context ->
                            if (NetworkHelper.hasNetworkAccess(context)) {
                                EETrackerPreferenceManager.getUserEmail(context)?.let { emailId ->
                                    viewModel?.changePassword(emailId, oldPassword, newPassword)
                                }
                            }
                        }
                    } else {
                        confirmNewPassword.error = getString(R.string.txt_error_confirm_pwd)
                    }
                }
            }
        }
    }
}
