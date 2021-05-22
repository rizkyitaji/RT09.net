package com.mobile.spanduk.ui.home.report

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mobile.spanduk.R
import com.mobile.spanduk.databinding.ActivityAddReportBinding
import com.mobile.spanduk.ui.home.report.guest.LaporTamuActivity
import com.mobile.spanduk.ui.home.report.self.LaporDiriActivity
import com.mobile.spanduk.ui.home.report.warga.ReportActivity
import com.mobile.spanduk.utils.Connection
import com.mobile.spanduk.utils.Preferences
import dmax.dialog.SpotsDialog
import java.util.*

class AddReportActivity : AppCompatActivity() {

    lateinit var id: String
    lateinit var name: String
    lateinit var date: String
    lateinit var type: String
    lateinit var detail: String
    lateinit var filePath: Uri
    lateinit var mIntent: Intent
    lateinit var pref: Preferences
    lateinit var imgRef: StorageReference
    lateinit var mDatabase: DatabaseReference
    private lateinit var bind: ActivityAddReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityAddReportBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val net = Connection(this)
        pref = Preferences(this)

        mDatabase = FirebaseDatabase.getInstance().getReference("Report")

        val report = intent.getStringExtra("report")
        if (report.equals("warga")) {
            type = "Aduan"
            bind.tvTitle.text = getString(R.string.aduan_warga)
            mIntent = Intent(this, ReportActivity::class.java)
        } else if (report.equals("tamu")) {
            type = "Lapor Tamu"
            bind.tvTitle.text = getString(R.string.lapor_tamu)
            mIntent = Intent(this, LaporTamuActivity::class.java)
        } else {
            type = "Lapor Diri"
            bind.tvTitle.text = getString(R.string.lapor_diri)
            mIntent = Intent(this, LaporDiriActivity::class.java)
        }

        if (pref.getValues("action").equals("edit")) {
            id = pref.getValues("id").toString()
            val uri = pref.getValues("uri").toString()

            bind.tvDate.text = pref.getValues("date")
            bind.etName.setText(pref.getValues("name"))
            bind.etDetail.setText(pref.getValues("detail"))

            Glide.with(this)
                .load(uri)
                .apply(RequestOptions.centerCropTransform())
                .into(bind.ivPhoto)

            bind.ivCamera.visibility = View.GONE
        } else {
            bind.etName.setText(pref.getValues("nameLogin"))
            id = mDatabase.push().key.toString()
        }

        imgRef = FirebaseStorage.getInstance().getReference("images/$id")

        bind.btnBack.setOnClickListener {
            onBackPressed()
        }

        bind.tvDate.setOnClickListener {
            pickDate()
        }

        bind.ivPhoto.setOnClickListener {
            if (net.isOnline()) {
                ImagePicker.with(this)
                    .galleryOnly()
                    .crop()
                    .start()
            } else {
                net.isOffline()
            }
        }

        bind.btnSubmit.setOnClickListener {
            name = bind.etName.text.toString()
            date = bind.tvDate.text.toString().trim()
            detail = bind.etDetail.text.toString()

            if (name == "") {
                bind.etName.error = "Lengkapi isian terlebih dahulu!"
                bind.etName.requestFocus()
            } else if (detail == "") {
                bind.etDetail.error = "Lengkapi isian terlebih dahulu!"
                bind.etDetail.requestFocus()
            } else if (date == "Pilih Tanggal") {
                Toast.makeText(this, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_SHORT).show()
            } else if (bind.ivCamera.visibility == View.VISIBLE) {
                Toast.makeText(this, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_SHORT).show()
            } else {
                if (net.isOnline()) {
                    saveData()
                } else {
                    net.isOffline()
                }
            }
        }
    }

    private fun pickDate() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, mYear, mMonth, mDay ->
            val textDate = "$mDay-${mMonth + 1}-$mYear"
            bind.tvDate.text = textDate
        }, year, month, day)
        datePicker.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            filePath = data?.data!!
            Glide.with(this)
                .load(filePath)
                .apply(RequestOptions.centerCropTransform())
                .into(bind.ivPhoto)
            bind.ivCamera.visibility = View.GONE
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Dibatalkan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveData() {
        val dialog = SpotsDialog.Builder()
            .setContext(this)
            .setCancelable(false)
            .build()

        dialog.show()

        imgRef.putFile(filePath)
            .addOnProgressListener { taskSnapshot ->
                pref.setValues("proses", "1")
                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                dialog.setMessage("Uploaded " + progress.toInt() + "%")
            }

            .addOnFailureListener { e ->
                dialog.dismiss()
                Toast.makeText(this, "Failed " + e.message, Toast.LENGTH_SHORT).show()
            }

            .addOnSuccessListener {
                imgRef.downloadUrl.addOnSuccessListener {
                    val ref = mDatabase.child(id)
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            ref.child("id").setValue(id)
                            ref.child("name").setValue(name)
                            ref.child("date").setValue(date)
                            ref.child("type").setValue(type)
                            ref.child("detail").setValue(detail)
                            ref.child("photo").setValue(it.toString())

                            if (pref.getValues("action").equals("add")) {
                                ref.child("status").setValue("Menunggu")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@AddReportActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                }

                dialog.dismiss()
                pref.setValues("proses", "0")
                Toast.makeText(this, "Data telah diperbarui", Toast.LENGTH_SHORT).show()
                startActivity(mIntent)
            }
    }

    override fun onBackPressed() {
        if (pref.getValues("proses").equals("1")) {
            Toast.makeText(this, "Silakan tunggu sampai proses selesai", Toast.LENGTH_SHORT).show()
        } else {
            if (pref.getValues("tap").equals("0")) {
                pref.setValues("tap", "1")
                Toast.makeText(this, "Tap again to exit", Toast.LENGTH_SHORT).show()
            } else {
                pref.setValues("tap", "0")
                super.onBackPressed()
            }
        }
    }
}