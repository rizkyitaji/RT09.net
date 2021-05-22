package com.mobile.spanduk.ui.home.mail.incoming

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mobile.spanduk.R
import com.mobile.spanduk.databinding.ActivityDetailIncomingMailBinding
import com.mobile.spanduk.ui.home.HomeActivity
import com.mobile.spanduk.utils.Preferences

class DetailIncomingMailActivity : AppCompatActivity() {

    private lateinit var bind: ActivityDetailIncomingMailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityDetailIncomingMailBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val pref = Preferences(this)

        bind.btnBack.setOnClickListener {
            onBackPressed()
        }

        bind.mailNum.text = pref.getValues("no")
        bind.dateReceived.text = pref.getValues("tgl")
        bind.mailingDate.text = pref.getValues("date")
        bind.from.text = pref.getValues("from")
        bind.subject.text = pref.getValues("subject")
        bind.detail.text = pref.getValues("detail")
        bind.etc.text = pref.getValues("etc")

        bind.btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }
}