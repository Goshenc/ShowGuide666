// HotMovieFragment.kt
package com.example.filmguide

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filmguide.databinding.FragmentHotMovieBinding
import com.example.filmguide.logic.network.hotmovie.HotMovieClient
import com.example.filmguide.ui.HotMovieAdapter
import com.example.filmguide.MovieDetailActivity
import com.example.filmguide.utils.ToastUtil
import kotlinx.coroutines.launch

class HotMovieFragment : Fragment() {
    private var _binding: FragmentHotMovieBinding? = null
    private val binding get() = _binding!!
    private val adapter = HotMovieAdapter()

    companion object {
        fun newInstance(cityId: Int, cityName: String): HotMovieFragment {
            val args = Bundle().apply {
                putInt("cityId", cityId)
                putString("cityName", cityName)
            }
            return HotMovieFragment().apply { arguments = args }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHotMovieBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        adapter.setOnItemClickListener { movieId ->
            val intent = Intent(requireContext(), MovieDetailActivity::class.java)
            intent.putExtra("movieId", movieId)
            startActivity(intent)
        }

        val cityId = arguments?.getInt("cityId", -1) ?: -1
        val cityName = arguments?.getString("cityName") ?: ""

        if (cityId == -1) {
            ToastUtil.show(requireContext(), "无效城市 ID", R.drawable.icon)
            parentFragmentManager.popBackStack()
            return
        }

        lifecycleScope.launch {
            try {
                val response = HotMovieClient.hotMoviesApi.getHotMovies(4, cityId,cityName)
                val movieList = response.data.hotMovies
                movieList?.let { nonNullMovieList ->
                    adapter.submitList(nonNullMovieList)
                }
                Log.d("zxy", response.toString())
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.show(requireContext(), "加载失败：${e.message}", R.drawable.icon)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}