package com.example.filmguide.logic.model

data class Reminder(
    var year: Int,            // 年，例如 2025
    var month: Int,           // 月，0~11（与 Calendar 保持一致）
    var dayOfMonth: Int,      // 日
    var hourOfDay: Int,       // 时
    var minute: Int           // 分
) {
    var id: Int = 0           // 用来和 PendingIntent 一一对应的唯一标识
}
