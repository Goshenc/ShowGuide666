package com.example.filmguide.ui

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import android.view.View
import android.view.LayoutInflater
import android.widget.Button
import com.example.filmguide.R
import com.example.filmguide.ReminderActivity

class IsBuyDialogFragment : DialogFragment() {

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
            val intent = Intent(requireActivity(), ReminderActivity::class.java)
            startActivity(intent)
            dismiss()

        }

        return dialog
    }
}