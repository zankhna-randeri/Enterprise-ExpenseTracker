package com.avengers.enterpriseexpensetracker.util

class Constants {
    companion object {

        //Expense type
        enum class ExpenseType {
            Food,
            Travel,
            Accommodation,
            Other
        }

        enum class Status {
            Pending,
            Approved,
            Rejected
        }

        // SharedPreference
        const val PREFS_LOGIN = "is_login"
        const val PREFS_EMAIL = "email"
        const val PREFS_FNAME = "first_name"
        const val PREFS_LNAME = "last_name"

        // Actions
        const val ACTION_LOGIN = "com.avengers.enterpriseexpensetracker.action.LOGIN"
        const val ACTION_RECEIPT_SCAN = "com.avengers.enterpriseexpensetracker.action.UPLOAD"
        const val ACTION_SUBMIT_EXPENSE_REPORT =
            "com.avengers.enterpriseexpensetracker.action.SUBMIT_EXPENSE_REPORT"
        const val ACTION_FETCH_ALL_REPORTS =
            "com.avengers.enterpriseexpensetracker.action.ALL_REPORTS"

        // Extras
        const val EXTRA_LOGIN_USER = "login_user"
        const val EXTRA_UPLOAD_RECEIPT_PATH = "receipt_path"
        const val EXTRA_UPLOAD_EXPENSE_TYPE = "expense_type"
        const val EXTRA_SUBMIT_EXPENSE_REPORT = "expense_report"
        const val EXTRA_UPDATE_DEVICE_TOKEN = "device_token"
        const val EXTRA_API_RESPONSE = "api_response"

        // Broadcast
        const val BROADCAST_LOGIN_RESPONSE =
            "com.avengers.enterpriseexpensetracker.service.action.LOGIN_RESPONSE"
        const val BROADCAST_RECEIPT_SCAN_RESPONSE =
            "com.avengers.enterpriseexpensetracker.service.action.RECEIPT_SCAN_RESPONSE"
        const val BROADCAST_SUBMIT_EXPENSE_REPORT_RESPONSE =
            "com.avengers.enterpriseexpensetracker.service.action.SUBMIT_EXPENSE_REPORT_RESPONSE"
        const val BROADCAST_FETCH_ALL_REPORTS =
            "com.avengers.enterpriseexpensetracker.service.action.FETCH_ALL_REPORTS"
    }
}