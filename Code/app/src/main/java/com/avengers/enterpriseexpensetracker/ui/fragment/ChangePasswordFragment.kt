package com.avengers.enterpriseexpensetracker.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.viewmodel.ChangePasswordViewModel
import com.google.android.material.textfield.TextInputLayout

class ChangePasswordFragment : Fragment(), View.OnClickListener {

    private var viewModel: ChangePasswordViewModel? = null
    private lateinit var oldPassword: TextInputLayout
    private lateinit var newPassword: TextInputLayout
    private lateinit var confirmNewPassword: TextInputLayout
    private lateinit var btnSubmit: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(this).get(ChangePasswordViewModel::class.java)
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    private fun initView(view: View) {
        oldPassword = view.findViewById(R.id.txt_input_old_password)
        newPassword = view.findViewById(R.id.txt_input_new_password)
        confirmNewPassword = view.findViewById(R.id.txt_input_confirm_new_password)
        btnSubmit = view.findViewById(R.id.btn_change_pwd_submit)
        btnSubmit.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            btnSubmit.id -> {
                confirmNewPassword.error = null
                val oldPassword = oldPassword.editText?.text
                val newPassword = newPassword.editText?.text
                val confirmPassword = confirmNewPassword.editText?.text
                if (isAllFieldsValid(oldPassword, newPassword, confirmPassword)) {
                    if ((newPassword.toString() == confirmPassword.toString())) {
                    } else {
                        confirmNewPassword.error = getString(R.string.txt_error_confirm_pwd)
                    }
                }
            }
        }
    }

    private fun isAllFieldsValid(oldPassword: Editable?,
                                 newPassword: Editable?,
                                 confirmPassword: Editable?): Boolean {
        return !(oldPassword.isNullOrBlank()) && !(newPassword.isNullOrBlank()) &&
                !(confirmPassword.isNullOrBlank())
    }
}
