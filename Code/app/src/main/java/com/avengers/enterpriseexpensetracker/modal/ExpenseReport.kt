package com.avengers.enterpriseexpensetracker.modal

import android.os.Parcel
import android.os.Parcelable
import com.avengers.enterpriseexpensetracker.util.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ExpenseReport : Parcelable {
    private var reportId: String? = null
    private var reportName: String? = null
    private var emailId: String? = null
    private var expenseDetailsRequest: MutableList<Expense>? = null
    private var approvedDate: String? = null
    private var reportCreateDate: String? = null
    private var reportLastModified: String? = null
    private var reportStatus: String = Constants.Companion.Status.Pending.name
    private var reportTotalAmount: Double = 0.0

    constructor(parcel: Parcel) : this() {
        reportId = parcel.readString()
        reportName = parcel.readString()
        emailId = parcel.readString()
        approvedDate = parcel.readString()
        reportCreateDate = parcel.readString()
        reportLastModified = parcel.readString()
        reportStatus = parcel.readString() as String
        reportTotalAmount = parcel.readDouble()
        expenseDetailsRequest = ArrayList()
        parcel.readTypedList(expenseDetailsRequest!!, Expense.CREATOR)
    }

    constructor()

    fun setName(name: String) {
        this.reportName = name
    }

    fun getName(): String? {
        return reportName
    }

    fun getExpenses(): MutableList<Expense>? {
        return expenseDetailsRequest
    }

    fun setExpenses(expenses: MutableList<Expense>) {
        this.expenseDetailsRequest = expenses
    }

    override fun toString(): String {
        return "ExpenseReport(reportId=$reportId, " +
                "reportName=$reportName, " +
                "emailId=$emailId, " +
                "expenseDetailsRequest=$expenseDetailsRequest," +
                " approvalDate=$approvedDate," +
                " submissionDate=$reportCreateDate," +
                " reportLastModified=$reportLastModified," +
                " status=$reportStatus," +
                " total=$reportTotalAmount)"
    }

    fun setReportStatus(status: String) {
        this.reportStatus = status
    }

    fun setEmailId(emailId: String) {
        this.emailId = emailId
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(reportId)
        parcel.writeString(reportName)
        parcel.writeString(emailId)
        parcel.writeString(approvedDate)
        parcel.writeString(reportCreateDate)
        parcel.writeString(reportLastModified)
        parcel.writeString(reportStatus)
        parcel.writeDouble(reportTotalAmount)
        parcel.writeTypedList(expenseDetailsRequest)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ExpenseReport> {
        override fun createFromParcel(parcel: Parcel): ExpenseReport {
            return ExpenseReport(parcel)
        }

        override fun newArray(size: Int): Array<ExpenseReport?> {
            return arrayOfNulls(size)
        }
    }

    fun getReportStatus(): String {
        return reportStatus
    }

    fun getApprovedDate(): String {
        return formattedDate(approvedDate)
    }

    fun getCreationDate(): String {
        return formattedDate(reportCreateDate)
    }

    private fun formattedDate(dateString: String?): String {
        // convert to MMMM dd, yyyy format -> June 10, 2020
        dateString?.let {
            val dateFormatter = SimpleDateFormat("yyyy-mm-dd", Locale.US)
            val date = dateFormatter.parse(dateString)
            return SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(date!!)
        }

        return ""
    }

    fun getTotal(): Float {
        return reportTotalAmount.toFloat()
    }

    fun getReportId(): String? {
        return reportId
    }
}