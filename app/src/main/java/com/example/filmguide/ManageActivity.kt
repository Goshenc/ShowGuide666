package com.example.filmguide

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filmguide.databinding.ActivityMainBinding
import com.example.filmguide.databinding.ActivityManageBinding
import com.example.filmguide.logic.AppDatabase
import com.example.filmguide.logic.dao.PerformanceDao
import com.example.filmguide.logic.dao.MovieDao
import com.example.filmguide.logic.network.moviedetail.MovieEntity
import com.example.filmguide.logic.network.performancedetail.PerformanceEntity
import com.example.filmguide.ui.ManagerAdapter


class ManageActivity : AppCompatActivity() {

    private lateinit var binding : ActivityManageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityManageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val appDatabase = AppDatabase.getInstance(this@ManageActivity)
        val performanceDao = appDatabase.performanceDao()
        val movieDao = appDatabase.movieDao()



        val adapter = ManagerAdapter(performanceDao, movieDao,this@ManageActivity,binding.testView) {
                item ->
            val intent = Intent(this@ManageActivity, CreateRecordActivity::class.java)
            Log.d("zxy6","点击成功")
            if (item is PerformanceEntity) {
                intent.putExtra("id", item.performanceId)
                intent.putExtra("img",item.posterUrl)
                intent.putExtra("name",item.name)
            } else if (item is MovieEntity) {
                intent.putExtra("id", item.id)
                intent.putExtra("img",item.imageUrl)
                intent.putExtra("name",item.name)
            }
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }
}