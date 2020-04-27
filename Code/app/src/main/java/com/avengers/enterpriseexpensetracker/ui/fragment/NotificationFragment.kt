package com.avengers.enterpriseexpensetracker.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.viewmodel.NotificationViewModel

class NotificationFragment : Fragment() {

    private var viewModel: NotificationViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }
}
