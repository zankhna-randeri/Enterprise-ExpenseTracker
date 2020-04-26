package com.avengers.enterpriseexpensetracker.modal.response

import android.os.Parcel
import android.os.Parcelable

open class ApiResponse(private var statusToUI: Boolean = false,
                       private var message: String? = null) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readValue(Boolean::class.java.classLoader) as Boolean) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(statusToUI)
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

    fun getStatus(): Boolean {
        return statusToUI
    }

    fun getMessage(): String? {
        return message
    }

    override fun toString(): String {
        return "ApiResponse(statusToUI=$statusToUI, message=$message)"
    }

}
