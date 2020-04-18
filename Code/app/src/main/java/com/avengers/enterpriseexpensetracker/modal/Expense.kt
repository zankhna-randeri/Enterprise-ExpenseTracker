package com.avengers.enterpriseexpensetracker.modal

import android.os.Parcel
import android.os.Parcelable

class Expense() : Parcelable {
    private var emailId: Int = -1
    private var businessName: String? = null
    private var businessAddress: String? = null
    private var expenseCategory: String? = null
    private var expenseTotal: Float = 0f
    private var expenseDate: String? = null
    private var expenseTime: String? = null
    private var expenseSubCategory: String? = null
    private var receiptURL: String? = null

    constructor(parcel: Parcel) : this() {
        emailId = parcel.readInt()
        businessName = parcel.readString()
        businessAddress = parcel.readString()
        expenseCategory = parcel.readString()
        expenseTotal = parcel.readFloat()
        expenseDate = parcel.readString()
        expenseTime = parcel.readString()
        expenseSubCategory = parcel.readString()
        receiptURL = parcel.readString()
    }

    fun setAmount(amount: Float) {
        this.expenseTotal = amount
    }

    fun setDate(date: String) {
        this.expenseDate = date
    }

    fun getAmount(): Float {
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

    fun getBusinessName(): String {
        return if (businessName.isNullOrBlank()) {
            "N/A"
        } else {
            businessName as String
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(emailId)
        parcel.writeString(businessName)
        parcel.writeString(businessAddress)
        parcel.writeString(expenseCategory)
        parcel.writeFloat(expenseTotal)
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