package com.avengers.enterpriseexpensetracker.modal.tracking

data class TrackLoginData(private var method: String) {
    fun getMethod(): String {
        return method
    }
}