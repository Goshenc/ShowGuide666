package com.example.filmguide.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.filmguide.logic.network.user.ServiceCreator
import com.example.filmguide.logic.network.user.UserZ
import retrofit2.Call
import retrofit2.Response

class AuthViewModel : ViewModel() {

    // —— 注册相关 不变 ——
    private val _regResult = MutableLiveData<Boolean>()
    val regResult: LiveData<Boolean> = _regResult

    fun register(username: String, password: String, email: String) {
        val user = UserZ(id = null, username = username, password = password, email = email, createdAt = null)
        ServiceCreator.apiService.register(user)
            .enqueue(object : retrofit2.Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    android.util.Log.d("AUTH", "register code=${response.code()} body='${response.body() ?: ""}'")
                    _regResult.postValue(response.isSuccessful && response.body()?.contains("成功") == true)
                }
                override fun onFailure(call: Call<String>, t: Throwable) {
                    android.util.Log.e("AUTH", "register failure", t)
                    _regResult.postValue(false)
                }
            })
    }

    // —— 登录相关：改为 Boolean ——

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    /** 发起登录请求 */
    fun login(username: String, password: String) {
        ServiceCreator.apiService.login(username, password)
            .enqueue(object : retrofit2.Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    val raw = response.body() ?: ""
                    val body = raw.trim()
                    // 打印原始和修剪后的响应，方便调试
                    android.util.Log.d("AUTH", "login onResponse: code=${response.code()} raw='${raw}' trimmed='${body}'")
                    val success = response.isSuccessful && body == "登录成功"
                    android.util.Log.d("AUTH", "login 判定 success=$success")
                    _loginSuccess.postValue(success)
                }
                override fun onFailure(call: Call<String>, t: Throwable) {
                    android.util.Log.e("AUTH", "login failure", t)
                    _loginSuccess.postValue(false)
                }
            })
    }
}