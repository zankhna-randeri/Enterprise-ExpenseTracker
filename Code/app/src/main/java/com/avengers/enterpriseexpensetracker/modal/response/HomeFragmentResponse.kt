package com.avengers.enterpriseexpensetracker.modal.response

import com.avengers.enterpriseexpensetracker.modal.ExpenseReport

data class HomeFragmentResponse(var categoryWiseExpense: CategoryWiseTotalResponse?,
                                var expenseReports: MutableList<ExpenseReport>?) : ApiResponse() {

    constructor() : this(null, null)
}

data class GetAllReportsResponse(var reports: List<ExpenseReport>?) : ApiResponse()
