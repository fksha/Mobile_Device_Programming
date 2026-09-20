package com.example.bugs_android.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bugs_android.fragments.AuthorsFragment
import com.example.bugs_android.fragments.RegistrationFragment
import com.example.bugs_android.fragments.RulesFragment
import com.example.bugs_android.fragments.SettingsFragment

class ViewPagerAdapter(activity: FragmentActivity) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RegistrationFragment()
            1 -> RulesFragment()
            2 -> AuthorsFragment()
            3 -> SettingsFragment()
            else -> RegistrationFragment()
        }
    }
}