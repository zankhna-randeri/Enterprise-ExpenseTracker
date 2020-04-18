package com.avengers.enterpriseexpensetracker.modal

class ExpenseReport {
    private var reportId: Int? = -1
    private var reportName: String? = null
    private var emailId: String? = null
    private var expenseDetailsRequest: MutableList<Expense>? = null
    private var approvalDate: String? = null
    private var submissionDate: String? = null
    private var status: String? = null
    private var total: Float? = 0f

    init {
        expenseDetailsRequest = ArrayList()
    }

    constructor(id: Int?,
                reportName: String?,
                emailId: String?,
                expenses: MutableList<Expense>?,
                approvalDate: String?,
                submissionDate: String?,
                status: String?,
                total: Float?) {
        this.reportId = id
        this.reportName = reportName
        this.emailId = emailId
        this.expenseDetailsRequest = expenses
        this.approvalDate = approvalDate
        this.submissionDate = submissionDate
        this.status = status
        this.total = total
    }

    constructor()

    fun setName(name: String) {
        this.reportName = name
    }

}