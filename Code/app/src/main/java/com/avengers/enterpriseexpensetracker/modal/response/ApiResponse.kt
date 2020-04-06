package com.avengers.enterpriseexpensetracker.modal.response

import android.os.Parcel
import android.os.Parcelable

open class ApiResponse(protected var responseCode: Int? = null,
                       protected var status: Boolean? = null) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readValue(Boolean::class.java.classLoader) as? Boolean) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(responseCode)
        parcel.writeValue(status)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ApiResponse> {
        override fun createFromParcel(parcel: Parcel): ApiResponse {
            return ApiResponse(parcel)
        }

        override fun newArray(size: Int): Array<ApiResponse?> {
            return arrayOfNulls(size)
        }
    }
}