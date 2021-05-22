package com.mobile.spanduk.ui.home.mail.outgoing

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.mobile.spanduk.R
import com.mobile.spanduk.databinding.ActivityDetailOutgoingMailBinding
import com.mobile.spanduk.ui.home.HomeActivity
import com.mobile.spanduk.utils.Preferences

class DetailOutgoingMailActivity : AppCompatActivity() {

    private lateinit var bind: ActivityDetailOutgoingMailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityDetailOutgoingMailBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val pref = Preferences(this)

        bind.btnBack.setOnClickListener {
            onBackPressed()
        }

        if (pref.getValues("mail").equals("warga")) {
            bind.tvTitle.text = getString(R.string.surat)
            bind.colon1.visibility = View.GONE
            bind.mailNum.visibility = View.GONE
            bind.tvNumber.visibility = View.GONE
        } else {
            bind.tvTitle.text = getString(R.string.surat_keluar)
            bind.tvNumber.text = pref.getValues("no")
        }

        bind.tvNik.text = pref.getValues("nik")
        bind.tvTtl.text = pref.getValues("ttl")
        bind.tvJob.text = pref.getValues("job")
        bind.tvName.text = pref.getValues("name")
        bind.tvDate.text = pref.getValues("date")
        bind.tvGender.text = pref.getValues("gender")
        bind.tvAddress.text = pref.getValues("address")
        bind.tvSubject.text = pref.getValues("subject")
        bind.tvReligion.text = pref.getValues("religion")

        bind.btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }
}