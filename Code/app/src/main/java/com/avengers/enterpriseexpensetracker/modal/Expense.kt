package com.avengers.enterpriseexpensetracker.modal

class Expense {
    private var id: Int = -1
    private var category: String? = null
    private var amount: Float = 0f
    private var date: String? = null

    constructor(id: Int, category: String?, amount: Float) {
        this.id = id
        this.category = category
        this.amount = amount
    }

    constructor()

    fun setAmount(amount: Float) {
        this.amount = amount
    }

    fun setDate(date: String?) {
        this.date = date
    }

    fun getAmount(): Float {
        return this.amount
    }

    fun getDate(): String? {
        return this.date
    }
}