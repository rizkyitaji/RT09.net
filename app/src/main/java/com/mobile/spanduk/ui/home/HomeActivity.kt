package com.mobile.spanduk.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mobile.spanduk.R
import com.mobile.spanduk.databinding.ActivityHomeBinding
import com.mobile.spanduk.ui.home.info.InformasiActivity
import com.mobile.spanduk.ui.home.list.DaftarWargaActivity
import com.mobile.spanduk.ui.home.mail.MailActivity
import com.mobile.spanduk.ui.home.profile.ProfileActivity
import com.mobile.spanduk.ui.home.report.guest.LaporTamuActivity
import com.mobile.spanduk.ui.home.report.self.LaporDiriActivity
import com.mobile.spanduk.ui.home.report.warga.ReportActivity
import com.mobile.spanduk.ui.home.schedule.JadwalActivity
import com.mobile.spanduk.ui.home.setting.SettingActivity
import com.mobile.spanduk.ui.home.user.UserActivity
import com.mobile.spanduk.ui.sign.SignInActivity
import com.mobile.spanduk.utils.Connection
import com.mobile.spanduk.utils.Preferences

class HomeActivity : AppCompatActivity() {

    lateinit var net: Connection
    lateinit var pref: Preferences
    private lateinit var bind: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(bind.root)

        net = Connection(this)
        pref = Preferences(this)

        val uri = pref.getValues("photo").toString()

        if (pref.getValues("level").equals("Admin")) {
            bind.ivSetting.visibility = View.GONE
            bind.tvName.text = getString(R.string.selamat_datang)
            bind.ivProfile.setImageResource(R.drawable.ic_logo)
        } else {
            bind.cvUser.visibility = View.GONE
            bind.cvLogout.visibility = View.GONE
            bind.tvName.text = pref.getValues("nameLogin")

            if (net.isOnline()) {
                if (!uri.equals("null")) {
                    Glide.with(this)
                        .load(uri)
                        .apply(RequestOptions.circleCropTransform())
                        .into(bind.ivProfile)
                }
            } else {
                net.isOffline()
            }

            bind.ivProfile.setOnClickListener {
                pref.setValues("action", "profile")
                startActivity(Intent(this, ProfileActivity::class.java))
            }
        }

        bind.tvLevel.text = pref.getValues("level").toString()

        bind.ivSetting.setOnClickListener {
            pref.setValues("action", "home")
            startActivity(Intent(this, SettingActivity::class.java))
        }

        bind.cvSchedule.setOnClickListener {
            startActivity(Intent(this, JadwalActivity::class.java))
        }

        bind.cvInfo.setOnClickListener {
            startActivity(Intent(this, InformasiActivity::class.java))
        }

        bind.cvReportSelf.setOnClickListener {
            startActivity(Intent(this, LaporDiriActivity::class.java))
        }

        bind.cvReportGuest.setOnClickListener {
            startActivity(Intent(this, LaporTamuActivity::class.java))
        }

        bind.cvMail.setOnClickListener {
            startActivity(Intent(this, MailActivity::class.java))
        }

        bind.cvList.setOnClickListener {
            startActivity(Intent(this, DaftarWargaActivity::class.java))
        }

        bind.cvReport.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }

        bind.cvUser.setOnClickListener {
            startActivity(Intent(this, UserActivity::class.java))
        }

        bind.cvLogout.setOnClickListener {
            finishAffinity()
            pref.setValues("status", "0")
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }

    override fun onBackPressed() {
        if (pref.getValues("tap").equals("0")) {
            pref.setValues("tap", "1")
            Toast.makeText(this, "Tap again to exit", Toast.LENGTH_SHORT).show()
        } else if (pref.getValues("tap").equals("1")) {
            pref.setValues("tap", "0")
            finishAffinity()
        } else {
            pref.setValues("tap", "0")
            super.onBackPressed()
        }
    }
}