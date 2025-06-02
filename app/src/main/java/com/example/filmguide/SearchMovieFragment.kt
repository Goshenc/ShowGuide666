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
import com.example.filmguide.databinding.FragmentSearchMoviesBinding
import com.example.filmguide.logic.network.searchmovies.SearchMovieClient
import com.example.filmguide.ui.MovieDetailActivity
import com.example.filmguide.ui.SearchMoviesAdapter
import com.example.filmguide.utils.ToastUtil
import kotlinx.coroutines.launch
import java.net.URLEncoder

class SearchMovieFragment : Fragment() {
    private var _binding: FragmentSearchMoviesBinding? = null
    private val binding get() = _binding!!
    private val adapter = SearchMoviesAdapter()

    companion object {
        fun newInstance(cityId: Int, keyword: String): SearchMovieFragment {
            val args = Bundle().apply {
                putInt("cityId", cityId)
                putString("keyword", keyword)
            }
            return SearchMovieFragment().apply { arguments = args }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchMoviesBinding.inflate(inflater, container, false)
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
        val rawKeyword = arguments?.getString("keyword") ?: ""
        val keyword = URLEncoder.encode(rawKeyword, "UTF-8")

        if (cityId == -1 || keyword.isEmpty()) {
            ToastUtil.show(requireContext(), "无效城市 ID 或关键词", R.drawable.icon)
            parentFragmentManager.popBackStack()
            return
        }

        loadMovies(cityId, rawKeyword)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadMovies( cityId: Int,keyword: String){
        lifecycleScope.launch {
            try {
                val response = SearchMovieClient.searchMovieApi.searchMovies(cityId, keyword)
                Log.d("zxy4",response.toString())
                val movieList = response.movies

                val listToSubmit = movieList ?: emptyList()
                adapter.submitList(listToSubmit)
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.show(requireContext(), "加载失败：${e.message}", R.drawable.icon)
            }
        }
    }

}