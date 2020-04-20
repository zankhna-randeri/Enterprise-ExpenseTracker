package com.avengers.enterpriseexpensetracker.modal.response

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class CategoryWiseTotalResponse(
    @SerializedName("accomadationTotalExpense")
    private var accommodationExpense: Float = 0f,
    private var otherTotalExpense: Float = 0f,
    private var travelTotalExpense: Float = 0f,
    private var foodTotalExpense: Float = 0f,
    private var emailId: String?) : ApiResponse(), Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readFloat(),
            parcel.readFloat(),
            parcel.readFloat(),
            parcel.readFloat(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeFloat(accommodationExpense)
        parcel.writeFloat(otherTotalExpense)
        parcel.writeFloat(travelTotalExpense)
        parcel.writeFloat(foodTotalExpense)
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

    fun getApiResponseStatus(): Boolean? {
        return super.getStatus()
    }
}