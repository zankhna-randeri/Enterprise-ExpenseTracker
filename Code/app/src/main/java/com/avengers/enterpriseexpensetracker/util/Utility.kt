package com.avengers.enterpriseexpensetracker.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.avengers.enterpriseexpensetracker.service.LoginService

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
       LoginService.enqueueWork(context, intent)
    }
}