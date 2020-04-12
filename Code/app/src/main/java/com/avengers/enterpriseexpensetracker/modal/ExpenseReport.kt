package com.avengers.enterpriseexpensetracker.modal

class ExpenseReport {
    private var id: Int? = -1
    private var expenses: MutableList<Expense>? = null
    private var approvalDate: String? = null
    private var submissionDate: String? = null
    private var status: String? = null
    private var total: Float? = 0f

    constructor(id: Int?,
                expenses: MutableList<Expense>?,
                approvalDate: String?,
                submissionDate: String?,
                status: String?,
                total: Float?) {
        this.id = id
        this.expenses = expenses
        this.approvalDate = approvalDate
        this.submissionDate = submissionDate
        this.status = status
        this.total = total
    }
}