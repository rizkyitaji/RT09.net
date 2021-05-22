package com.mobile.spanduk.ui.home.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mobile.spanduk.R
import com.mobile.spanduk.databinding.ActivityDetailUserBinding
import com.mobile.spanduk.ui.home.profile.ProfileActivity
import com.mobile.spanduk.utils.Preferences
import com.mobile.spanduk.utils.ShowImage

class DetailUserActivity : AppCompatActivity() {

    private lateinit var bind: ActivityDetailUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityDetailUserBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val img = ShowImage(this)
        val pref = Preferences(this)

        val uri = pref.getValues("uri").toString()
        val name = pref.getValues("name").toString()
        val pass = pref.getValues("pass").toString()
        val uname = pref.getValues("uname").toString()
        val phone = pref.getValues("phone").toString()
        val wa = "0" + pref.getValues("whatsapp")

        bind.btnBack.setOnClickListener {
            onBackPressed()
        }

        if (!uri.equals("")) {
            Glide.with(this)
                .load(uri)
                .apply(RequestOptions.circleCropTransform())
                .into(bind.ivPhoto)
        }

        bind.ivPhoto.setOnClickListener {
            img.show(uri, "blue")
        }

        bind.tvName.text = name
        bind.tvUsername.text = uname
        bind.tvPassword.text = pass
        bind.tvPhone.text = phone
        bind.tvWhatsapp.text = wa

        bind.btnView.setOnClickListener {
            pref.setValues("action", "visit")
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}