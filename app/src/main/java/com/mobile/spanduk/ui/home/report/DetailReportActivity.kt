package com.mobile.spanduk.ui.home.report

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mobile.spanduk.R
import com.mobile.spanduk.databinding.ActivityDetailReportBinding
import com.mobile.spanduk.utils.Preferences
import com.mobile.spanduk.utils.ShowImage

class DetailReportActivity : AppCompatActivity() {

    private lateinit var bind: ActivityDetailReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityDetailReportBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val pref = Preferences(this)

        bind.btnBack.setOnClickListener {
            onBackPressed()
        }

        val report = intent.getStringExtra("report")
        if (report.equals("warga")) {
            bind.tvTitle.text = getString(R.string.aduan_warga)
        } else if (report.equals("tamu")) {
            bind.tvTitle.text = getString(R.string.lapor_tamu)
        } else {
            bind.tvTitle.text = getString(R.string.lapor_diri)
        }

        val uri = pref.getValues("uri").toString()
        val name = "Dilaporkan oleh " + pref.getValues("name")
        val date = "Pada tanggal "+ pref.getValues("date")

        bind.tvName.text = name
        bind.tvDate.text = date
        bind.tvDetail.text = pref.getValues("detail")

        Glide.with(this)
            .load(uri)
            .apply(RequestOptions.centerCropTransform())
            .into(bind.ivPhoto)

        bind.ivPhoto.setOnClickListener {
            val img = ShowImage(this)
            img.show(uri, "white")
        }
    }
}