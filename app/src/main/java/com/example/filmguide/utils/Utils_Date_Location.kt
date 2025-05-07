package com.example.filmguide.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils_Date_Location {
    // 日期相关方法
    private const val DEFAULT_DATE_FORMAT = "yyyy-MM-dd"

    private fun getDateFormat(pattern: String): SimpleDateFormat =
        SimpleDateFormat(pattern, Locale.getDefault())

    fun formatDate(date: Date, pattern: String = DEFAULT_DATE_FORMAT): String =
        getDateFormat(pattern).format(date)

    fun getCurrentDate(pattern: String = DEFAULT_DATE_FORMAT): String =
        formatDate(Date(), pattern)

    // 位置相关方法
    class LocationHelper(private val context: Context) {
        private val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        private var locationListener: LocationListener? = null

        /*
         * 获取位置：
         * 1. 检查位置权限；
         * 2. 尝试获取最后已知的位置；
         * 3. 注册位置更新监听器，获取最新位置后调用回调并取消更新。
         */

        /*在这段代码中，onLocationReceived 并不是你在类内部单独定义的一个方法，而是作为参数声明在 getLocation 方法的签名中。也就是说，它是一个回调函数，当你调用 getLocation 时，需要传入这个回调函数的具体实现。

        举个例子，在你的 Activity 或其他调用方中，你会这样使用：

        kotlin
        复制
        编辑
        val locationHelper = Utils_Date_Location.LocationHelper(this)
        locationHelper.getLocation { location ->
            // 这里就是你定义 onLocationReceived 的地方
            Log.d("MainActivity", "获取到位置: 经度=${location.longitude}, 纬度=${location.latitude}")
        }
        在这个调用中，{ location -> ... } 就是 onLocationReceived 的实现。因此，你是在调用 getLocation 的地方定义了这个回调函数，而不是在 LocationHelper 类内部。*/
        @SuppressLint("MissingPermission")
        fun getLocation(onLocationReceived: (Location) -> Unit) {//
            // 检查权限
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("LocationHelper", "位置权限未授予")
                return
            }

            // 控制只调用一次回调的标志
            var locationReceived = false
            fun deliverLocation(location: Location) {
                if (!locationReceived) {
                    locationReceived = true
                    onLocationReceived(location)// // 这里调用回调，把 location 传出去,即传到AddDiaryActivity中
                    removeLocationUpdates()  // 位置获取成功后，取消监听
                }
            }

            // 尝试获取最后已知位置（优先GPS，其次网络）
            val lastKnownLocation: Location? = try {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } catch (e: SecurityException) {
                Log.e("LocationHelper", "获取最后位置异常: ${e.message}", e)
                null
            }
            lastKnownLocation?.let { deliverLocation(it) }//如果有缓存的 lastKnownLocation，会立刻执行
            //如果没有缓存位置，会注册 LocationListener，监听新的位置变化，当 GPS 或网络获取到位置时：
            // 注册位置更新监听器
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    deliverLocation(location)
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            // 请求GPS位置更新
            try {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000L, // 每 5 秒更新一次
                    10f,   // 位置变化超过 10 米才触发
                    locationListener ?: return
                )
            } catch (e: SecurityException) {
                Log.e("LocationHelper", "请求GPS位置更新异常: ${e.message}", e)
            }
            // 请求网络位置更新
            try {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5000L,
                    10f,
                    locationListener ?: return
                )
            } catch (e: SecurityException) {
                Log.e("LocationHelper", "请求网络位置更新异常: ${e.message}", e)
            }
        }


        /**
         * 移除位置更新
         */
        fun removeLocationUpdates() {
            locationListener?.let {
                locationManager.removeUpdates(it)
                locationListener = null
            }
        }
    }
}
