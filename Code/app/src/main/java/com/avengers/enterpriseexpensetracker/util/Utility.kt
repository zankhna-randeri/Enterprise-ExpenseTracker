package com.avengers.enterpriseexpensetracker.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.avengers.enterpriseexpensetracker.service.EETrackerJobService

class Utility {

    companion object {
        private var instance: Utility? = null

        @Synchronized
        fun getInstance(): Utility {
            if (instance == null) {
                instance = Utility()
            }
            return instance!!
        }
    }

    fun showMsg(context: Context, msg: String?) {
        Toast.makeText(context, msg,
                Toast.LENGTH_SHORT).show()
    }

    fun startExpenseTrackerService(context: Context?, intent: Intent?) {
        EETrackerJobService.enqueueWork(context, intent)
    }

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
