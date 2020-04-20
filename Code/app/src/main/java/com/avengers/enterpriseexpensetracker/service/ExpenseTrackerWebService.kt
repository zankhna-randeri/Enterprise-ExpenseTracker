package com.avengers.enterpriseexpensetracker.service

import com.avengers.enterpriseexpensetracker.modal.ExpenseReport
import com.avengers.enterpriseexpensetracker.modal.LoginUser
import com.avengers.enterpriseexpensetracker.modal.response.ApiResponse
import com.avengers.enterpriseexpensetracker.modal.response.CategoryWiseTotalResponse
import com.avengers.enterpriseexpensetracker.modal.response.LoginResponse
import com.avengers.enterpriseexpensetracker.modal.response.ReceiptScanResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ExpenseTrackerWebService {
    companion object {
        private const val BASE_URL = "http://expensetracker.us-east-1.elasticbeanstalk.com"

        val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    @POST("/userLogin")
    fun loginUser(@Body userLoginRequest: LoginUser): Call<LoginResponse>

    @Multipart
    @POST("/receiptScan")
    fun uploadReceipt(@Part file: MultipartBody.Part,
                      @Part("emailId") emailId: RequestBody,
                      @Part("expenseCategory") category: RequestBody): Call<ReceiptScanResponse>

    @POST("/createReport")
    fun submitExpenseReport(@Body expenseReport: ExpenseReport): Call<ApiResponse>

    @GET("/categoryTotalAmountDetails")
    fun getCategoryWiseExpenseApproved(@Query("emailId") emailId: String): Call<CategoryWiseTotalResponse>

    @GET("/getAllReports")
    fun getAllExpenseReports(@Query("emailId") emailId: String): Call<List<ExpenseReport>>
}