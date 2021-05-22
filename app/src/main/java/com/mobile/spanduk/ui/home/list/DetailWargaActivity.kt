package com.mobile.spanduk.ui.home.list

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobile.spanduk.R
import com.mobile.spanduk.databinding.ActivityDetailWargaBinding
import com.mobile.spanduk.ui.home.HomeActivity
import com.mobile.spanduk.utils.Preferences

class DetailWargaActivity : AppCompatActivity() {

    private lateinit var bind: ActivityDetailWargaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityDetailWargaBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val pref = Preferences(this)

        bind.btnBack.setOnClickListener {
            onBackPressed()
        }

        bind.tvKk.text = pref.getValues("kk")
        bind.tvNik.text = pref.getValues("nik")
        bind.tvTtl.text = pref.getValues("ttl")
        bind.tvJob.text = pref.getValues("job")
        bind.tvName.text = pref.getValues("name")
        bind.tvPhone.text = pref.getValues("phone")
        bind.tvGender.text = pref.getValues("gender")
        bind.tvAddress.text = pref.getValues("address")
        bind.tvReligion.text = pref.getValues("religion")
        bind.tvEducation.text = pref.getValues("education")

        bind.btnHome.setOnClickListener {
            pref.setValues("tap", "2")
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }
}