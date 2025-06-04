package com.example.filmguide

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.view.LayoutInflater
import android.widget.Button
import androidx.fragment.app.DialogFragment

class IsBuyDialogFragment : DialogFragment() {
    interface OnConfirmButtonClickListener {
        fun onConfirmClick() // 可根据需求添加参数
    }

    // 声明接口变量并提供默认实现（避免空指针）
    private var onConfirmClickListener: OnConfirmButtonClickListener? = null

    // 允许外部设置回调的方法
    fun setOnConfirmButtonClickListener(listener: OnConfirmButtonClickListener) {
        onConfirmClickListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view: View = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_dialog_isbuy, null)
        val builder = AlertDialog.Builder(requireContext())

        builder.setView(view)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val cancelButton: Button = view.findViewById(R.id.dialog_cancel)
        val confirmButton: Button = view.findViewById(R.id.dialog_confirm)

        cancelButton.setOnClickListener {
            dismiss()
        }

        confirmButton.setOnClickListener {

            onConfirmClickListener?.onConfirmClick()
            val intent = Intent(requireActivity(), ManageActivity::class.java)
            startActivity(intent)
            dismiss()

        }

        return dialog
    }
}