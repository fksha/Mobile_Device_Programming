package com.example.bugs_android.network
import android.util.Log
import java.text.SimpleDateFormat

import java.util.Date
import java.util.Locale

object GoldRateRepository {
    suspend fun getGoldRate(): GoldRate? {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val today = dateFormat.format(Date())

        val response = RetrofitClient.goldRateService.getGoldRate(today, today)

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