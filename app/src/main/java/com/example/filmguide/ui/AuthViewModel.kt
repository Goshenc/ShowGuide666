package com.example.filmguide.ui

import androidx.lifecycle.*
import com.example.filmguide.logic.AuthRepository
import com.example.filmguide.logic.model.User
import kotlinx.coroutines.launch

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {

    /** 注册结果 */
    private val _regResult = MutableLiveData<Boolean>()
    val regResult: LiveData<Boolean> = _regResult

    /** 登录结果：成功时发出 User，否则发出 null */
    private val _loginResult = MutableLiveData<User?>()
    val loginResult: LiveData<User?> = _loginResult

    fun register(account: String, pwd: String) = viewModelScope.launch {
        _regResult.value = repo.register(account, pwd)
    }

    fun login(account: String, pwd: String) = viewModelScope.launch {
        _loginResult.value = repo.login(account, pwd)
    }
}
