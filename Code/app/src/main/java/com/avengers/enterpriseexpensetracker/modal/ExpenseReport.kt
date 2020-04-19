package com.avengers.enterpriseexpensetracker.modal

import android.os.Parcel
import android.os.Parcelable

class ExpenseReport : Parcelable {
    private var reportId: Int? = -1
    private var reportName: String? = null
    private var emailId: String? = null
    private var expenseDetailsRequest: MutableList<Expense>? = null
    private var approvalDate: String? = null
    private var submissionDate: String? = null
    private var reportStatus: String? = null
    private var total: Float? = 0f

    constructor(parcel: Parcel) : this() {
        reportId = parcel.readValue(Int::class.java.classLoader) as? Int
        reportName = parcel.readString()
        emailId = parcel.readString()
        approvalDate = parcel.readString()
        submissionDate = parcel.readString()
        reportStatus = parcel.readString()
        total = parcel.readValue(Float::class.java.classLoader) as? Float
    }

    init {
        expenseDetailsRequest = ArrayList()
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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(reportId)
        parcel.writeString(reportName)
        parcel.writeString(emailId)
        parcel.writeString(approvalDate)
        parcel.writeString(submissionDate)
        parcel.writeString(reportStatus)
        parcel.writeValue(total)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "ExpenseReport(reportId=$reportId, " +
                "reportName=$reportName, " +
                "emailId=$emailId, " +
                "expenseDetailsRequest=$expenseDetailsRequest," +
                " approvalDate=$approvalDate," +
                " submissionDate=$submissionDate," +
                " status=$reportStatus," +
                " total=$total)"
    }

    companion object CREATOR : Parcelable.Creator<ExpenseReport> {
        override fun createFromParcel(parcel: Parcel): ExpenseReport {
            return ExpenseReport(parcel)
        }

        override fun newArray(size: Int): Array<ExpenseReport?> {
            return arrayOfNulls(size)
        }
    }

    fun setReportStatus(status: String) {
        this.reportStatus = status
    }

    fun setEmailId(emailId: String) {
        this.emailId = emailId
    }
}