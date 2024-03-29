package com.avengers.enterpriseexpensetracker.modal.request

import android.os.Parcel
import android.os.Parcelable

data class LoginUser(private var emailId: String? = null,
                     private var password: String? = null) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(emailId)
        parcel.writeString(password)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LoginUser> {
        override fun createFromParcel(parcel: Parcel): LoginUser {
            return LoginUser(parcel)
        }

        override fun newArray(size: Int): Array<LoginUser?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "LoginUser(emailId=$emailId, password=$password)"
    }
}
