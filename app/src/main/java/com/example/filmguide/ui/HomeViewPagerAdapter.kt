// ViewPagerAdapter.kt
package com.example.filmguide

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class HomeViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val cityId: Int,
    private val cityName: String
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3 // 后续可扩展

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllPerformanceFragment.newInstance()
            1 -> HotMovieFragment.newInstance(cityId, cityName)
            2 -> ExpectedMovieFragment.newInstance(cityId,cityName)
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}