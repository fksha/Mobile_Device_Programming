package com.example.bugs_android.network
import android.icu.util.Calendar
import android.util.Log
import java.text.SimpleDateFormat

import java.util.Date
import java.util.Locale

object GoldRateRepository {
    suspend fun getGoldRate(): GoldRate? {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        //val today = dateFormat.format(Date())

        //val response = RetrofitClient.goldRateService.getGoldRate(today, today)

        val calendar = Calendar.getInstance()
        val endDate = dateFormat.format(calendar.time)

        calendar.add(Calendar.DAY_OF_MONTH, -7)
        val startDate = dateFormat.format(calendar.time)

        val response = RetrofitClient.goldRateService.getGoldRate(startDate, endDate)

        //Log.d("GoldRate", "HTTP-код: ${response.code()}")

        if(!response.isSuccessful)
            return null

        val xml = response.body() ?: return null
        //Log.d("GoldRate", "XML: $xml")

        //Log.d("GoldRate", "XML получен, длина: ${xml.length}")

        val rate = parseGoldRate(xml)
        //Log.d("GoldRate", "Курс золота: $rate")

        return rate
    }

}