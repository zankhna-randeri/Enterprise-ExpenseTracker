package com.avengers.enterpriseexpensetracker.modal.tracking

data class TrackScreenData(private var screenName: String) {
    fun getScreenName(): String {
        return screenName
    }
}
