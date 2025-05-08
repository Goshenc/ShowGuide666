package com.example.filmguide.logic.network.user

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

// com.example.app.network/ApiService.kt



// com.example.filmguide.logic.network.user.ApiService.kt
interface ApiService {
    @POST("user/register")
    fun register(@Body user: UserZ): Call<String>

    // 新增：登录接口，假设后端登录成功返回整个 UserZ 对象
    @FormUrlEncoded
    @POST("user/login")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<String>
}

