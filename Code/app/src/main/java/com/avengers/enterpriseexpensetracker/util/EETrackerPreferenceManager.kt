package com.avengers.enterpriseexpensetracker.util

import android.content.Context
import androidx.preference.PreferenceManager

class EETrackerPreferenceManager {
    companion object {
        fun saveLoginPrefs(emailId: String?, fname: String?, lname: String?, context: Context?) {
            //Set the email Id and first name and last name in shared preferences
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = sharedPreferences.edit()
            editor.putString(Constants.PREFS_EMAIL, emailId)
            editor.putString(Constants.PREFS_FNAME, fname)
            editor.putString(Constants.PREFS_LNAME, lname)
            // keep user logged in
            editor.putBoolean(Constants.PREFS_LOGIN, true)
            editor.apply()
        }

        fun getUserEmail(context: Context?): String? {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getString(Constants.PREFS_EMAIL, null)
        }

        fun logout(context: Context?) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = sharedPreferences.edit()
            editor.putBoolean(Constants.PREFS_LOGIN, false)
            editor.apply()
        }
    }
}