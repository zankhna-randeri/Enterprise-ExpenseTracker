package com.avengers.enterpriseexpensetracker.modal.response

import android.os.Parcel
import android.os.Parcelable

data class ReceiptScanResponse(private var expenseBusinessName: String? = null,
                               private var expenseBusinessAddress: String? = null,
                               private var total: Double = 0.0,
                               private var expenseDate: String? = null,
                               private var expenseTime: String? = null,
                               private var expenseCategory: String? = null,
                               private var expenseSubCategory: String? = null,
                               private var userEmailId: String? = null,
                               private var receiptURL: String? = null) : ApiResponse(), Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readValue(Double::class.java.classLoader) as Double,
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeString(expenseBusinessName)
        parcel.writeString(expenseBusinessAddress)
        parcel.writeValue(total)
        parcel.writeString(expenseDate)
        parcel.writeString(expenseTime)
        parcel.writeString(expenseCategory)
        parcel.writeString(expenseSubCategory)
        parcel.writeString(userEmailId)
        parcel.writeString(receiptURL)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ReceiptScanResponse> {
        override fun createFromParcel(parcel: Parcel): ReceiptScanResponse {
            return ReceiptScanResponse(parcel)
        }

        override fun newArray(size: Int): Array<ReceiptScanResponse?> {
            return arrayOfNulls(size)
        }
    }

    fun getApiResponseStatus(): Boolean? {
        return super.getStatus()
    }

    fun getTotal(): Double {
        return this.total
    }

    fun getExpenseDate(): String? {
        return this.expenseDate
    }

    fun getBusinessName(): String? {
        return expenseBusinessName
    }

    fun getCategory(): String? {
        return expenseCategory
    }

    fun getBusinessAddress(): String? {
        return expenseBusinessAddress
    }

    fun getSubCategory(): String? {
        return expenseSubCategory
    }

    fun getExpenseTime(): String? {
        return expenseTime
    }

    fun getReceiptUrl(): String? {
        return receiptURL
    }
}
