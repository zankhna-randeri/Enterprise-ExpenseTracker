package com.avengers.enterpriseexpensetracker.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.avengers.enterpriseexpensetracker.R
import com.google.android.material.textfield.TextInputLayout

class ChangePasswordFragment : Fragment(), View.OnClickListener {

    private lateinit var oldPassword: TextInputLayout
    private lateinit var newPassword: TextInputLayout
    private lateinit var confirmNewPassword: TextInputLayout
    private lateinit var btnSubmit: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
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

}
