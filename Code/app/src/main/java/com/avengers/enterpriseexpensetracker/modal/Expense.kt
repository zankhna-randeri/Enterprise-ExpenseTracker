package com.avengers.enterpriseexpensetracker.modal

import android.os.Parcel
import android.os.Parcelable

class Expense() : Parcelable {
    private var emailId: String? = null
    private var businessName: String? = null
    private var businessAddress: String? = null
    private var expenseCategory: String? = null
    private var expenseTotal: Double = 0.0
    private var expenseDate: String? = null
    private var expenseTime: String? = null
    private var expenseSubCategory: String? = null
    private var receiptURL: String? = null

    constructor(parcel: Parcel) : this() {
        emailId = parcel.readString()
        businessName = parcel.readString()
        businessAddress = parcel.readString()
        expenseCategory = parcel.readString()
        expenseTotal = parcel.readDouble()
        expenseDate = parcel.readString()
        expenseTime = parcel.readString()
        expenseSubCategory = parcel.readString()
        receiptURL = parcel.readString()
    }

    constructor(email: String?,
                businessName: String?,
                address: String?,
                category: String?,
                subCategory: String?,
                total: Double,
                date: String?,
                time: String?,
                url: String?) : this() {
        this.emailId = email
        this.businessName = businessName
        this.businessAddress = address
        this.expenseCategory = category
        this.expenseSubCategory = subCategory
        this.expenseTotal = total
        this.expenseDate = date
        this.expenseTime = time
        this.receiptURL = url
    }

    fun setAmount(amount: Double) {
        this.expenseTotal = amount
    }

    fun setDate(date: String) {
        this.expenseDate = date
    }

    fun getAmount(): Double {
        return this.expenseTotal
    }

    fun getDate(): String? {
        return this.expenseDate
    }

    fun setCategory(category: String) {
        this.expenseCategory = category
    }

    fun getCategory(): String? {
        return expenseCategory
    }

    fun setBusinessName(name: String) {
        this.businessName = name
    }

    fun getBusinessName(): String? {
        return businessName
    }

    fun getSubCategory(): String? {
        return expenseSubCategory
    }

    fun getBusinessAddress(): String? {
        return businessAddress
    }

    fun setEmailId(emailId: String) {
        this.emailId = emailId
    }

    fun setBusinessAddress(address: String) {
        this.businessAddress = address
    }

    fun setExpenseTime(time: String) {
        this.expenseTime = time
    }

    fun setSubCategory(subCategory: String) {
        this.expenseSubCategory = subCategory
    }

    fun setReceiptUrl(url: String) {
        this.receiptURL = url
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(emailId)
        parcel.writeString(businessName)
        parcel.writeString(businessAddress)
        parcel.writeString(expenseCategory)
        parcel.writeDouble(expenseTotal)
        parcel.writeString(expenseDate)
        parcel.writeString(expenseTime)
        parcel.writeString(expenseSubCategory)
        parcel.writeString(receiptURL)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Expense> {
        override fun createFromParcel(parcel: Parcel): Expense {
            return Expense(parcel)
        }

        override fun newArray(size: Int): Array<Expense?> {
            return arrayOfNulls(size)
        }
    }
}