package com.avengers.enterpriseexpensetracker.modal.request

import android.os.Parcel
import android.os.Parcelable

data class ChangePasswordRequest(private var emailId: String,
                                 private var oldPassword: String, private var newPassword: String) :
    Parcelable {

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

    companion object CREATOR : Parcelable.Creator<ChangePasswordRequest> {
        override fun createFromParcel(parcel: Parcel): ChangePasswordRequest {
            return ChangePasswordRequest(parcel)
        }

        override fun newArray(size: Int): Array<ChangePasswordRequest?> {
            return arrayOfNulls(size)
        }
    }
}