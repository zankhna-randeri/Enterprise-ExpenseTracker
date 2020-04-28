package com.avengers.enterpriseexpensetracker.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.adapter.NotificationAdapter
import com.avengers.enterpriseexpensetracker.modal.Notification
import com.avengers.enterpriseexpensetracker.util.EETrackerPreferenceManager
import com.avengers.enterpriseexpensetracker.viewmodel.NotificationViewModel

class NotificationFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private var viewModel: NotificationViewModel? = null
    private lateinit var notificationView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initObservers()
        getNotifications()
    }

    private fun initObservers() {
        viewModel?.getApiCallStatus()?.observe(viewLifecycleOwner, Observer { notifications ->
            swipeRefreshLayout.isRefreshing = false
            if (notifications.isNullOrEmpty()) {
                //TODO: Show empty view
            } else {
                bindNotifications(notifications)
            }
        })
    }

    private fun bindNotifications(notifications: MutableList<Notification>) {
        val adapter = NotificationAdapter(notifications)
        notificationView.adapter = adapter
    }

    private fun getNotifications() {
        swipeRefreshLayout.isRefreshing = true
        EETrackerPreferenceManager.getUserEmail(context)?.let { viewModel?.getAllNotifications(it) }
    }

    private fun initView(view: View) {
        notificationView = view.findViewById(R.id.notificationView)
        notificationView.layoutManager = LinearLayoutManager(activity)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshNotification)
        swipeRefreshLayout.setOnRefreshListener(this)
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                R.color.color_chart_1,
                R.color.color_chart_3)
    }

    override fun onRefresh() {
        getNotifications()
    }
}
