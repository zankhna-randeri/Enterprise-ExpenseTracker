package com.avengers.enterpriseexpensetracker.util

import android.content.Context
import android.os.Bundle
import com.avengers.enterpriseexpensetracker.modal.tracking.TrackLoginData
import com.avengers.enterpriseexpensetracker.modal.tracking.TrackScreenData
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsHelper {
    companion object {
        private var instance: AnalyticsHelper? = null
        private lateinit var firebaseAnalytics: FirebaseAnalytics

        @Synchronized
        fun getInstance(): AnalyticsHelper {
            if (instance == null) {
                instance = AnalyticsHelper()
            }
            return instance!!
        }
    }

    fun trackViewScreenEvent(context: Context, trackingData: TrackScreenData) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        val bundle = Bundle()
        bundle.putString("screenName", trackingData.getScreenName())
        firebaseAnalytics.logEvent("view_screen", bundle)
    }

    fun trackLogin(context: Context, trackingData: TrackLoginData) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, trackingData.getMethod())
        })
    }

    fun trackForgotPassword(context: Context, trackingData: TrackLoginData) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        firebaseAnalytics.logEvent("click_event", Bundle().apply {
            putString("button_click", trackingData.getMethod())
        })
    }
}
