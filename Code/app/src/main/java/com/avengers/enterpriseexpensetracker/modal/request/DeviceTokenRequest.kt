package com.avengers.enterpriseexpensetracker.modal.request

import android.os.Parcel
import android.os.Parcelable

data class DeviceTokenRequest(val emailId: String, val deviceToken: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString() as String,
            parcel.readString() as String) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(emailId)
        parcel.writeString(deviceToken)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DeviceTokenRequest> {
        override fun createFromParcel(parcel: Parcel): DeviceTokenRequest {
            return DeviceTokenRequest(parcel)
        }

        override fun newArray(size: Int): Array<DeviceTokenRequest?> {
            return arrayOfNulls(size)
        }
    }


}
