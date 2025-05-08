package com.example.filmguide.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * 因为 AuthViewModel 现在是无参构造，所以 Factory 直接返回它的实例，无需传入 AuthRepository
 */
class AuthViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
