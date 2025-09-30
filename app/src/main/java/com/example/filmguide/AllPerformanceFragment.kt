package com.example.filmguide

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filmguide.databinding.FragmentAllPerformanceBinding
import com.example.filmguide.databinding.FragmentHotMovieBinding
import com.example.filmguide.logic.network.allperformance.AllPerformanceClient
import com.example.filmguide.logic.network.hotmovie.HotMovieClient
import com.example.filmguide.ui.AllPerformanceAdapter
import com.example.filmguide.ui.HotMovieAdapter
import com.example.filmguide.MovieDetailActivity
import com.example.filmguide.utils.ToastUtil
import kotlinx.coroutines.launch

class AllPerformanceFragment : Fragment() {
    private val adapter = AllPerformanceAdapter()
    private var _binding: FragmentAllPerformanceBinding ?= null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(): AllPerformanceFragment {
            val args = Bundle().apply {

            }
            return AllPerformanceFragment().apply { arguments = args }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllPerformanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        adapter.setOnItemClickListener { performance ->
            val intent = Intent(requireContext(), PerformanceDetailActivity::class.java)
            intent.putExtra("performance", performance)
            startActivity(intent)
        }


        lifecycleScope.launch {
            try {
                val response = AllPerformanceClient.allPerformanceApi.getPerformances()
                val performanceList = response.data
                adapter.submitList(performanceList)
                Log.d("zxy3",response.toString())
                Log.d("zxy3", performanceList.toString())
                Log.d("zxy3",performanceList.size.toString())
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