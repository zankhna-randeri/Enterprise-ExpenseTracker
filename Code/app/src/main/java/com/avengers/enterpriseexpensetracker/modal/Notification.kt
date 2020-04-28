package com.avengers.enterpriseexpensetracker.modal

import android.os.Parcel
import android.os.Parcelable

data class Notification(private var notificationId: Int = 0,
                        private var emailId: String = "",
                        private var reportID: String = "",
                        private var notificatonMessage: String = "",
                        private var createDate: String = "") : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString() as String,
            parcel.readString() as String,
            parcel.readString() as String,
            parcel.readString() as String) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(notificationId)
        parcel.writeString(emailId)
        parcel.writeString(reportID)
        parcel.writeString(notificatonMessage)
        parcel.writeString(createDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Notification> {
        override fun createFromParcel(parcel: Parcel): Notification {
            return Notification(parcel)
        }

        override fun newArray(size: Int): Array<Notification?> {
            return arrayOfNulls(size)
        }
    }
}