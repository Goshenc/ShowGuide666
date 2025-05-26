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
import com.example.filmguide.databinding.FragmentSearchPerformanceBinding
import com.example.filmguide.logic.network.searchperformance.SearchPerformanceClient
import com.example.filmguide.ui.SearchPerformanceAdapter
import com.example.filmguide.utils.ToastUtil
import kotlinx.coroutines.launch
import java.net.URLEncoder

class SearchPerformanceFragment : Fragment() {
    private var _binding: FragmentSearchPerformanceBinding? = null
    private val binding get() = _binding!!
    private val adapter = SearchPerformanceAdapter()

    companion object {
        fun newInstance(keyword: String): SearchPerformanceFragment {
            val args = Bundle().apply {
                putString("keyword", keyword)
            }
            return SearchPerformanceFragment().apply { arguments = args }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchPerformanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
//        adapter.setOnItemClickListener { performanceId ->
//            val intent = Intent(requireContext(), PerformanceDetailActivity::class.java)
//            intent.putExtra("performanceId", performanceId)
//            startActivity(intent)
//        }

        val rawKeyword = arguments?.getString("keyword") ?: ""
        val keyword = URLEncoder.encode(rawKeyword, "UTF-8")

        if (keyword.isEmpty()) {
            ToastUtil.show(requireContext(), "无效关键词", R.drawable.icon)
            parentFragmentManager.popBackStack()
            return
        }

        loadPerformances(keyword)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadPerformances(keyword: String) {
        lifecycleScope.launch {
            try {
                val response = SearchPerformanceClient.searchPerformanceApi.searchPerformances(keyword = keyword)
                val performanceList = response.getEnhancedPerformances()
                Log.d("zxy",performanceList.toString())
                val listToSubmit = performanceList ?: emptyList()
                adapter.submitList(listToSubmit)
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.show(requireContext(), "加载失败：${e.message}", R.drawable.icon)
            }
        }
    }
}