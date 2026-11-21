package com.example.bugs_android.network

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {
    private val retrofit = Retrofit.Builder().baseUrl("https://www.cbr.ru/").addConverterFactory(
        ScalarsConverterFactory.create()).build()
    //ScalarsConverterFactory — позволяет получить XML в виде обычной строки.

    val goldRateService: GoldRateService = retrofit.create(GoldRateService::class.java)
}