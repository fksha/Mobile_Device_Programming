package com.example.bugs_android

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.bugs_android.adapter.ViewPagerAdapter
import com.example.bugs_android.logic.getZodiacImage
import com.example.bugs_android.logic.сalculateZodiac

import com.example.bugs_android.model.Player
import com.example.bugs_android.network.GoldRateRepository
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : FragmentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        lifecycleScope.launch {
//            val rate = GoldRateRepository.getGoldRate()
//            Log.d("GoldRate", "Результат: $rate")
//        }

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager){
            tab, position ->
            tab.text = when(position){
                0 -> "Регистрация"
                1 -> "Правила"
                2 -> "Игра"
                3 -> "Авторы"
                4 -> "Настройки"
                5 -> "Рекорды"
                else -> ""
            }
        }.attach()
    }
}

