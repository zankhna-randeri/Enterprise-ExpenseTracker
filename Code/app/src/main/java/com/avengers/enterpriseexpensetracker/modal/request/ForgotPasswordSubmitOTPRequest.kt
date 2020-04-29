package com.avengers.enterpriseexpensetracker.modal.request

import android.os.Parcel
import android.os.Parcelable

data class ForgotPasswordSubmitOTPRequest(val emailId: String,
                                          val oldPassword: String,
                                          val newPassword: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString() as String,
            parcel.readString() as String,
            parcel.readString() as String) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(emailId)
        parcel.writeString(oldPassword)
        parcel.writeString(newPassword)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ForgotPasswordSubmitOTPRequest> {
        override fun createFromParcel(parcel: Parcel): ForgotPasswordSubmitOTPRequest {
            return ForgotPasswordSubmitOTPRequest(parcel)
        }

        override fun newArray(size: Int): Array<ForgotPasswordSubmitOTPRequest?> {
            return arrayOfNulls(size)
        }
    }

}