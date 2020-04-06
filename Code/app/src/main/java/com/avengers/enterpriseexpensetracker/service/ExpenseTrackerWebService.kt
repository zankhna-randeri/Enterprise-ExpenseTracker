package com.avengers.enterpriseexpensetracker.service

import com.avengers.enterpriseexpensetracker.modal.LoginUser
import com.avengers.enterpriseexpensetracker.modal.response.LoginResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ExpenseTrackerWebService {
    companion object {
        private const val BASE_URL = "https://sumanthravipati-sjsu.online"

        val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    @POST("/userLogin")
    fun loginUser(@Body userLoginRequest: LoginUser): Call<LoginResponse>
}