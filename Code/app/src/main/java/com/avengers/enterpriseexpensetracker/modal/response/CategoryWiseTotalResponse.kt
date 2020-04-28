package com.avengers.enterpriseexpensetracker.modal.response

import android.os.Parcel
import android.os.Parcelable

data class CategoryWiseTotalResponse(
    private var accommodationTotalExpense: Double = 0.0,
    private var otherTotalExpense: Double = 0.0,
    private var travelTotalExpense: Double = 0.0,
    private var foodTotalExpense: Double = 0.0,
    private var emailId: String?) : ApiResponse(), Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeDouble(accommodationTotalExpense)
        parcel.writeDouble(otherTotalExpense)
        parcel.writeDouble(travelTotalExpense)
        parcel.writeDouble(foodTotalExpense)
        parcel.writeString(emailId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CategoryWiseTotalResponse> {
        override fun createFromParcel(parcel: Parcel): CategoryWiseTotalResponse {
            return CategoryWiseTotalResponse(parcel)
        }

        override fun newArray(size: Int): Array<CategoryWiseTotalResponse?> {
            return arrayOfNulls(size)
        }
    }

    fun getFoodExpense(): Float {
        return foodTotalExpense.toFloat()
    }

    fun getAccommodationExpense(): Float {
        return accommodationTotalExpense.toFloat()
    }

    fun getOtherExpense(): Float {
        return otherTotalExpense.toFloat()
    }

    fun getTravelExpense(): Float {
        return travelTotalExpense.toFloat()
    }
}
