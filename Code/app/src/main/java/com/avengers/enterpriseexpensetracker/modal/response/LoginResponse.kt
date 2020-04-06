package com.avengers.enterpriseexpensetracker.modal.response

import android.os.Parcel
import android.os.Parcelable

data class LoginResponse(private var message: String? = null,
                         private var firstName: String? = null,
                         private var lastName: String? = null,
                         private var emailId: String?) : ApiResponse(), Parcelable {


    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    companion object CREATOR : Parcelable.Creator<LoginResponse> {
        override fun createFromParcel(parcel: Parcel): LoginResponse {
            return LoginResponse(parcel)
        }

        override fun newArray(size: Int): Array<LoginResponse?> {
            return arrayOfNulls(size)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(message)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(emailId)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getMessage(): String? {
        return this.message
    }


    fun getEmail(): String? {
        return this.emailId
    }

    fun getFname(): String? {
        return this.firstName
    }

    fun getLname(): String? {
        return this.lastName
    }

    fun getApiResponseCode(): Int? {
        return super.responseCode
    }

    fun getApiResponseStatus(): Boolean? {
        return super.status
    }
}