package com.avengers.enterpriseexpensetracker.service

import com.avengers.enterpriseexpensetracker.modal.ExpenseReport
import com.avengers.enterpriseexpensetracker.modal.request.ChangePasswordRequest
import com.avengers.enterpriseexpensetracker.modal.request.DeviceTokenRequest
import com.avengers.enterpriseexpensetracker.modal.request.LoginUser
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
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Url

interface EETrackerWebService {
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
    fun getAllExpenseReports(@Query("emailId") emailId: String): Call<MutableList<ExpenseReport>>

    @Headers("x-requested-with: xhr")
    @GET
    fun submitReportLambda(@Url url: String, @Query("first_name") param: String): Call<Void>

    @DELETE("/deleteReport")
    fun deleteReport(@Query("reportID") reportId: String): Call<ApiResponse>

    @POST("/updateDeviceId")
    fun updateDeviceToken(@Body updateDeviceIdRequest: DeviceTokenRequest): Call<ApiResponse>

    @POST("/userPasswordReset")
    fun changePassword(@Body userPwdResetRequest: ChangePasswordRequest): Call<ApiResponse>

    @POST("/filterReportsByDate")
    fun filterReportsByDate(@Query("emailId") emailId: String,
                            @Query("fromDate") fromDate: String,
                            @Query("toDate") toDate: String): Call<MutableList<ExpenseReport>>
}
