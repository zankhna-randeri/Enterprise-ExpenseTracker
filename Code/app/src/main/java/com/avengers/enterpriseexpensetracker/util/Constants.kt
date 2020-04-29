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

        const val TAG = "EETracker *******"

        // SharedPreference
        const val PREFS_LOGIN = "is_login"
        const val PREFS_EMAIL = "email"
        const val PREFS_FNAME = "first_name"
        const val PREFS_LNAME = "last_name"
        const val PREFS_DEVICE_TOKEN = "device_token"

        // Actions
        const val ACTION_LOGIN = "com.avengers.enterpriseexpensetracker.action.LOGIN"
        const val ACTION_RECEIPT_SCAN = "com.avengers.enterpriseexpensetracker.action.UPLOAD"
        const val ACTION_SUBMIT_EXPENSE_REPORT =
            "com.avengers.enterpriseexpensetracker.action.SUBMIT_EXPENSE_REPORT"
        const val ACTION_FETCH_ALL_REPORTS =
            "com.avengers.enterpriseexpensetracker.action.ALL_REPORTS"
        const val ACTION_UPDATE_DEVICE_TOKEN =
            "com.avengers.enterpriseexpensetracker.action.UPDATE_DEVICE_TOKEN"
        const val ACTION_FILTER_REPORTS_BY_DATE =
            "com.avengers.enterpriseexpensetracker.action.FILTER_REPORTS_BY_DATE"
        const val ACTION_REQUEST_OTP =
            "com.avengers.enterpriseexpensetracker.action.REQUEST_OTP"

        // Extras
        const val EXTRA_LOGIN_USER = "login_user"
        const val EXTRA_RECEIPT_PATH = "receipt_path"
        const val EXTRA_EXPENSE_TYPE = "expense_type"
        const val EXTRA_EXPENSE_REPORT = "expense_report"
        const val EXTRA_DEVICE_TOKEN = "device_token"
        const val EXTRA_API_RESPONSE = "api_response"
        const val EXTRA_APP_DATA_BUNDLE = "app_data_bundle"
        const val EXTRA_FROM_DATE = "from_date"
        const val EXTRA_TO_DATE = "to_date"
        const val EXTRA_EMAIL = "email"
        const val EXTRA_REQUEST_OTP = "request_otp"

        // Broadcast
        const val BROADCAST_LOGIN_RESPONSE =
            "com.avengers.enterpriseexpensetracker.service.action.BROADCAST_LOGIN_RESPONSE"
        const val BROADCAST_RECEIPT_SCAN_RESPONSE =
            "com.avengers.enterpriseexpensetracker.service.action.BROADCAST_RECEIPT_SCAN_RESPONSE"
        const val BROADCAST_SUBMIT_EXPENSE_REPORT_RESPONSE =
            "com.avengers.enterpriseexpensetracker.service.action.BROADCAST_SUBMIT_EXPENSE_REPORT_RESPONSE"
        const val BROADCAST_FETCH_ALL_REPORTS =
            "com.avengers.enterpriseexpensetracker.service.action.BROADCAST_FETCH_ALL_REPORTS"
        const val BROADCAST_UPDATE_DEVICE_TOKEN =
            "com.avengers.enterpriseexpensetracker.service.action.BROADCAST_UPDATE_DEVICE_TOKEN"
        const val BROADCAST_FILTER_REPORTS_BY_DATE =
            "com.avengers.enterpriseexpensetracker.service.action.BROADCAST_FILTER_REPORTS_BY_DATE"
        const val BROADCAST_REQUEST_OTP =
            "com.avengers.enterpriseexpensetracker.service.action.BROADCAST_REQUEST_OTP"

        // Firebase
        const val FCM_PAYLOAD_REPORT_ID = "reportId"
        const val FCM_PAYLOAD_REPORT_STATUS = "reportStatus"
    }
}
