package com.avengers.enterpriseexpensetracker.modal

class Expense {
    private var id: Int? = -1
    private var category: String? = null
    private var amount: Float? = 0f

    constructor(id: Int?, category: String?, amount: Float?) {
        this.id = id
        this.category = category
        this.amount = amount
    }
}