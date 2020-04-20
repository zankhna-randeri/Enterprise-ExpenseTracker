package com.avengers.enterpriseexpensetracker.modal.response

import com.avengers.enterpriseexpensetracker.modal.ExpenseReport

data class HomeFragmentResponse(var categoryWiseExpense: CategoryWiseTotalResponse?,
                                var expenseReports: List<ExpenseReport>?) : ApiResponse()
