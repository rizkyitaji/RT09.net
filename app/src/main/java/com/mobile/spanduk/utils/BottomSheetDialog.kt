package com.mobile.spanduk.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mobile.spanduk.R

class BottomSheetDialog (val context: Context) {

    lateinit var dialog : BottomSheetDialog

    fun build(layout: Int) : View {
        dialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(layout, null)
        dialog.setContentView(view)
        dialog.setCancelable(false)
        dialog.show()

        return view
    }

    fun menu() : BottomSheetDialog {
        return dialog
    }
}