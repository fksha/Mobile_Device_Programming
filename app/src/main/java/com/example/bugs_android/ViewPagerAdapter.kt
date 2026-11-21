package com.example.bugs_android.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bugs_android.fragments.AuthorsFragment
import com.example.bugs_android.fragments.GameFragment
import com.example.bugs_android.fragments.RecordsFragment
import com.example.bugs_android.fragments.RegistrationFragment
import com.example.bugs_android.fragments.RulesFragment
import com.example.bugs_android.fragments.SettingsFragment
import com.example.bugs_android.fragments.TournamentFragment
import com.example.bugs_android.fragments.TournamentResultsFragment

class ViewPagerAdapter(activity: FragmentActivity) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 8
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RegistrationFragment()
            1 -> RulesFragment()
            2 -> GameFragment()
            3 -> TournamentFragment()
            4 -> TournamentResultsFragment()
            3 -> AuthorsFragment()
            4 -> SettingsFragment()
            5 -> RecordsFragment()
            else -> RegistrationFragment()
        }
    }
}