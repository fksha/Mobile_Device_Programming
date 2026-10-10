package com.example.bugs_android.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.bugs_android.R

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_settings,
            container,
            false
        )

        val textViewSpeed = view.findViewById<TextView>(R.id.textViewSpeed)
        val seekBarSpeed = view.findViewById<SeekBar>(R.id.seekBarSpeed)

        setTextView(seekBarSpeed, textViewSpeed, "Скорость игры", "speed")

        val textViewCountBugs = view.findViewById<TextView>(R.id.textViewCountBugs)
        val seekBarCountBugs = view.findViewById<SeekBar>(R.id.seekBarCountBugs)

        setTextView(seekBarCountBugs, textViewCountBugs, "Максимальное количество жуков на экране", "maxBugs")

        val textViewBonusInterval = view.findViewById<TextView>(R.id.textViewBonusInterval)
        val seekBarBonusInterval = view.findViewById<SeekBar>(R.id.seekBarInterval)

        setTextView(seekBarBonusInterval, textViewBonusInterval, "Интервал появления бонусов (сек)", "bonusInterval")

        val textViewTimeRound = view.findViewById<TextView>(R.id.textViewTimeRound)
        val seekBarTimeRound = view.findViewById<SeekBar>(R.id.seekBarTimeRound)

        setTextView(seekBarTimeRound, textViewTimeRound, "Длительность раунда (сек)", "roundDuration")



        return view
    }
}

fun setTextView(seekBar: SeekBar, textView: TextView, text: String, key: String){
    val preferences = seekBar.context.getSharedPreferences(
        "game_settings",
        android.content.Context.MODE_PRIVATE
    )

    seekBar.progress = preferences.getInt(key, seekBar.progress)
    textView.text = "$text: ${seekBar.progress}"

    seekBar.setOnSeekBarChangeListener(
        object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                textView.text = "$text: $progress"
                preferences.edit()
                    .putInt(key, progress)
                    .apply()

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
    )
}