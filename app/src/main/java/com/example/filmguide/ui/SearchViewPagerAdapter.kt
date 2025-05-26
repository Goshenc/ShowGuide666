package com.example.filmguide.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.filmguide.SearchMovieFragment
import com.example.filmguide.SearchPerformanceFragment
import com.example.filmguide.logic.network.searchperformance.SearchPerformance

class SearchViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val cityId: Int,
    private var keyword: String
) : FragmentStateAdapter(fragmentActivity) {

    private val updateVersions = longArrayOf(0L, 1L)

    override fun getItemCount(): Int = 2

    override fun getItemId(position: Int): Long {
        return updateVersions[position]
    }

    override fun containsItem(itemId: Long): Boolean {
        return itemId in updateVersions
    }

    fun updateKeyword(newKeyword: String) {
        keyword = newKeyword
        updateVersions[0]++
        updateVersions[1]++
        notifyDataSetChanged()
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SearchMovieFragment.newInstance(cityId, keyword)
            1 -> SearchPerformanceFragment.newInstance(keyword)
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}