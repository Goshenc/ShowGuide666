package com.example.filmguide.logic.model

data class Reminder(
    var hourOfDay: Int,
    var minute: Int,
    var id: Int = 0
) {
    override fun toString(): String {
        // %02d 表示两位，不够补 0
        return String.format("%02d:%02d", hourOfDay, minute)
    }
}

