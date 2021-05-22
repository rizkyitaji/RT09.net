package com.mobile.spanduk.utils

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View

class AlertDialog (val context: Context) {

    lateinit var dialog : AlertDialog

    fun build(layout: Int) : View {
        val builder = AlertDialog.Builder(context)
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(layout, null)
        builder.setView(view)

        dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        return view
    }

    fun menu() : AlertDialog {
        return dialog
    }
}