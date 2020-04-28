package com.avengers.enterpriseexpensetracker.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.adapter.NotificationAdapter
import com.avengers.enterpriseexpensetracker.modal.Notification
import com.avengers.enterpriseexpensetracker.ui.widget.SwipeToDeleteCallback
import com.avengers.enterpriseexpensetracker.util.EETrackerPreferenceManager
import com.avengers.enterpriseexpensetracker.util.NetworkHelper
import com.avengers.enterpriseexpensetracker.util.Utility
import com.avengers.enterpriseexpensetracker.viewmodel.NotificationViewModel

class NotificationFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private var viewModel: NotificationViewModel? = null
    private lateinit var notificationView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: TextView
    private var swipeToDeleteCallback: SwipeToDeleteCallback? = null
    private var adapter: NotificationAdapter? = null

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
        enableSwipeToDelete()
    }

    private fun enableSwipeToDelete() {
        activity?.applicationContext?.let { context ->

            swipeToDeleteCallback = object : SwipeToDeleteCallback(context) {

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
                    val position = viewHolder.adapterPosition
                    if (NetworkHelper.hasNetworkAccess(context)) {
                        viewModel?.deleteNotification(position)
                    }
                }
            }

            swipeToDeleteCallback?.let { swipeCallback ->
                val itemTouchHelper = ItemTouchHelper(swipeCallback)
                itemTouchHelper.attachToRecyclerView(notificationView)
            }
        }
    }

    private fun initObservers() {
        viewModel?.getNotifications()?.observe(viewLifecycleOwner, Observer { notifications ->
            swipeRefreshLayout.isRefreshing = false
            if (notifications.isNullOrEmpty()) {
                showEmptyView()
            } else {
                hideEmptyView()
                if (adapter != null) {
                    adapter?.notifyDataSetChanged()
                } else {
                    bindNotifications(notifications)
                }
            }
        })

        viewModel?.getDeleteResponse()?.observe(viewLifecycleOwner, Observer { response ->
            if (!response.isSuccess()) {
                activity?.applicationContext?.let {
                    Utility.getInstance().showMsg(it, it.getString(R.string.txt_api_failed))
                }
            }
        })
    }

    private fun bindNotifications(notifications: List<Notification>) {
        adapter = NotificationAdapter(notifications)
        notificationView.adapter = adapter
    }

    private fun getNotifications() {
        swipeRefreshLayout.isRefreshing = true
        EETrackerPreferenceManager.getUserEmail(context)?.let { viewModel?.getAllNotifications(it) }
    }

    private fun initView(view: View) {
        emptyView = view.findViewById(R.id.emptyView)
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

    private fun showEmptyView() {
        emptyView.text = getString(R.string.txt_empty_notification)
        emptyView.visibility = View.VISIBLE
        notificationView.visibility = View.GONE
    }

    private fun hideEmptyView() {
        emptyView.visibility = View.GONE
        notificationView.visibility = View.VISIBLE
    }
}
