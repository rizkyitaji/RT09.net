package com.mobile.spanduk.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mobile.spanduk.R

class ShowImage(val context: Context) {

    val net = Connection(context)

    fun show(uri: String, bg: String) {
        val dialog = AlertDialog(context)
        val view = dialog.build(R.layout.layout_show_photo)
        val profile = view.findViewById<ZoomView>(R.id.iv_photo)

        if (bg.equals("white")) {
            profile.setBackgroundResource(R.color.white)
            profile.setImageResource(R.drawable.ic_profile_cyan)
        } else {
            profile.setBackgroundResource(R.color.blue)
            profile.setImageResource(R.drawable.ic_profile_white)
        }

        if (net.isOnline()) {
            if (!uri.equals("null")) {
                Glide.with(context)
                    .load(uri)
                    .apply(RequestOptions.centerCropTransform())
                    .into(profile)
            }
        }
    }
}