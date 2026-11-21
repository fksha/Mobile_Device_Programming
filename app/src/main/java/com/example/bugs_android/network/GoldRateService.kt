package com.example.bugs_android.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GoldRateService {

    @GET("scripts/xml_metall.asp")
    suspend fun getGoldRate(
        @Query("date_req1") startDate: String,
        @Query("date_req2") endDate: String
    ): Response<String>
}