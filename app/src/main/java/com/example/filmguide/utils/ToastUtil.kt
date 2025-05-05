package com.example.filmguide.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.filmguide.R

object ToastUtil {
    fun show(context: Context, msg: String, iconRes: Int? = null) {
        val inflater = LayoutInflater.from(context)
        val layout   = inflater.inflate(R.layout.toast_custom, null)
        layout.findViewById<TextView>(R.id.tvMessage).text = msg
        iconRes?.let {
            layout.findViewById<ImageView>(R.id.ivIcon).setImageResource(it)
        } ?: run {
            layout.findViewById<ImageView>(R.id.ivIcon).visibility = View.GONE
        }
        Toast(context).apply {
            view = layout
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 200)
            duration = Toast.LENGTH_SHORT
            show()
        }
    }
}
