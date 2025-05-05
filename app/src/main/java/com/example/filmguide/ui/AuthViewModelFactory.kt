package com.example.filmguide.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.filmguide.logic.AuthRepository


class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            // 创建仓库并传入 ViewModel
            val repository = AuthRepository(context)
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: \$modelClass")
    }
}
