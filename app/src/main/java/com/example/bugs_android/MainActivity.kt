package com.example.bugs_android

import android.os.Bundle
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
import com.example.bugs_android.logic.getZodiacImage
import com.example.bugs_android.logic.сalculateZodiac

import com.example.bugs_android.model.Player
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editTextFio = findViewById<EditText>(R.id.editTextFIO)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val courseSpinner = findViewById<Spinner>(R.id.spinnerCourse)
        val difficultySeekBar = findViewById<SeekBar>(R.id.seekBarDifficulty)

        val yearEditText = findViewById<EditText>(R.id.editTextYear)
        val setYearButton = findViewById<Button>(R.id.buttonSetYear)

        val dateCalendarView = findViewById<CalendarView>(R.id.calendarViewBirthDate)

        var birthDate = ""
        var birthDay = 0
        var birthMonth = 0
        var zodiac = ""
        dateCalendarView.setOnDateChangeListener { _, year, month, day ->
            birthDay = day
            birthMonth = month + 1
            birthDate = "$day.$birthMonth.$year"
            zodiac = сalculateZodiac(birthDay, birthMonth)
        }

        val zodiacButtonCalc = findViewById<Button>(R.id.buttonCalc)
        val zodiacImageView = findViewById<ImageView>(R.id.imageViewZodiac)

        val buttonSave = findViewById<Button>(R.id.buttonSave)
        val textViewRes = findViewById<TextView>(R.id.textViewResult)
        val zodiacImageViewRes = findViewById<ImageView>(R.id.imageView)

        setYearButton.setOnClickListener {
            val yearText = yearEditText.text.toString()
            if (yearText.isEmpty()){
                Toast.makeText(this, "Введите год!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val year = yearText.toInt()
            if(year < 1900 || year > 2026){
                Toast.makeText(this, "Введите год от 1900 до 2026!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = dateCalendarView.date

            calendar.set(Calendar.YEAR, year)
            dateCalendarView.date = calendar.timeInMillis
        }

        zodiacButtonCalc.setOnClickListener {
            val zodiacImage = getZodiacImage(zodiac)
            if(zodiacImage == 0){
                Toast.makeText(this, "Выберите дату рождения!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            zodiacImageView.setImageResource(zodiacImage)
            zodiacImageView.visibility = View.VISIBLE
        }

        buttonSave.setOnClickListener {
            val fio = editTextFio.text.toString()
            if (fio.isEmpty()){
                Toast.makeText(this, "Заполните ФИО!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedGenderId = radioGroup.checkedRadioButtonId
            val gender = when(selectedGenderId){
                R.id.radioButton_M -> "Мужской"
                R.id.radioButton_W -> "Женский"
                else -> {
                    Toast.makeText(this, "Выберите пол!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val courseText = courseSpinner.selectedItem.toString()
            val course = courseText.replace(" курс", "").toInt()
            val difficulty = difficultySeekBar.progress

            if(birthDate.isEmpty()){
                Toast.makeText(this, "Выберите дату рождения!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val player = Player(
                fio,
                gender,
                course,
                difficulty,
                birthDate,
                zodiac
            )
            textViewRes.text = "Данные пользователя\n" +
                    "ФИО: ${player.fio}\n" +
                    "Пол: ${player.gender}\n" +
                    "Курс: ${player.course}\n" +
                    "Сложность игры: ${player.difficulty}\n" +
                    "День рождения: ${player.birthDate}\n" +
                    "Знак зодиака: ${player.zodiac}"


            val zodiacImage = getZodiacImage(player.zodiac)
            if(zodiacImage == 0){
                Toast.makeText(this, "Выберите дату рождения!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            zodiacImageViewRes.setImageResource(zodiacImage)

            textViewRes.visibility = View.VISIBLE
            zodiacImageViewRes.visibility = View.VISIBLE
        }
    }
}

