package com.mobile.spanduk.ui.home.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.mobile.spanduk.R
import com.mobile.spanduk.databinding.ActivityProfileBinding
import com.mobile.spanduk.ui.home.HomeActivity
import com.mobile.spanduk.ui.home.setting.SettingActivity
import com.mobile.spanduk.utils.Connection
import com.mobile.spanduk.utils.Preferences
import com.mobile.spanduk.utils.ShowImage
import dmax.dialog.SpotsDialog
import io.github.hyuwah.draggableviewlib.Draggable
import io.github.hyuwah.draggableviewlib.makeDraggable

class ProfileActivity : AppCompatActivity() {

    lateinit var wa: String
    lateinit var id: String
    lateinit var uri: String
    lateinit var name: String
    lateinit var level: String
    lateinit var phone: String
    lateinit var pref: Preferences
    lateinit var ref: DatabaseReference
    private lateinit var bind: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val img = ShowImage(this)
        val net = Connection(this)
        pref = Preferences(this)

        if (pref.getValues("action").equals("profile")) {
            uri = pref.getValues("photo").toString()
            id = pref.getValues("username").toString()
            name = pref.getValues("nameLogin").toString()
            level = pref.getValues("level").toString()
            bind.ivSetting.visibility = View.VISIBLE
            bind.btnUpload.visibility = View.VISIBLE
        } else {
            uri = pref.getValues("uri").toString()
            id = pref.getValues("uname").toString()
            name = pref.getValues("name").toString()
            level = pref.getValues("user").toString()
            bind.ivSetting.visibility = View.INVISIBLE
            bind.btnUpload.visibility = View.INVISIBLE
        }

        bind.btnBack.setOnClickListener {
            onBackPressed()
        }

        bind.ivSetting.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }

        bind.ivProfile.setOnClickListener {
            img.show(uri, "white")
        }

        if (net.isOnline()) {
            if (!uri.equals("null")) {
                Glide.with(this)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(bind.ivProfile)
            }

            bind.tvName.text = name
            bind.tvLevel.text = level

            ref = FirebaseDatabase.getInstance().getReference("Users").child(id)
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val kk = snapshot.child("kk").value.toString()
                    phone = snapshot.child("phone").value.toString()
                    wa = snapshot.child("whatsapp").value.toString()

                    if (!kk.equals("null")) {
                        Glide.with(this@ProfileActivity)
                            .load(kk)
                            .apply(RequestOptions.centerCropTransform())
                            .into(bind.ivPhoto)

                        bind.tvNone.visibility = View.GONE

                        bind.ivPhoto.setOnClickListener {
                            img.show(kk, "white")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProfileActivity, ""+error.message, Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            net.isOffline()
        }

        bind.btnUpload.setOnClickListener {
            if (net.isOnline()) {
                ImagePicker.with(this)
                    .galleryOnly()
                    .crop()
                    .start()
            } else {
                net.isOffline()
            }
        }

        bind.btnContact.makeDraggable(Draggable.STICKY.AXIS_X, false)
        bind.btnContact.makeDraggable(Draggable.STICKY.AXIS_XY)
        bind.btnContact.makeDraggable()

        bind.btnPhone.setOnClickListener {
            if (net.isOnline()) {
                if (pref.getValues("action").equals("profile")) {
                    if (!phone.equals("null")){
                        Toast.makeText(this, "No. Telepon : " + phone, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Silakan tambahkan nomor telepon Anda terlebih dahulu!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (!phone.equals("null")) {
                        phoneCall()
                    } else {
                        Toast.makeText(this, "Nomor telepon tidak tersedia!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                net.isOffline()
            }
        }

        bind.btnWhatsapp.setOnClickListener {
            if (net.isOnline()) {
                if (pref.getValues("action").equals("profile")) {
                    if (!wa.equals("null")){
                        Toast.makeText(this, "No. Whatsapp : " + wa, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Silakan tambahkan nomor whatsapp Anda terlebih dahulu!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (!wa.equals("null")) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/=62" + wa)))
                    } else {
                        Toast.makeText(this, "Nomor whatsapp tidak tersedia!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                net.isOffline()
            }
        }
    }

    private fun phoneCall() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 1)
        } else {
            startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone)))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                phoneCall()
            } else {
                Toast.makeText(this, "PERMISSION DENIED", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val dialog = SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .build()

            dialog.show()
            val filePath = data?.data!!

            val imgRef = FirebaseStorage.getInstance().getReference("images/KK_$name")
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
                        ref.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                ref.child("kk").setValue(it.toString())
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@ProfileActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                            }
                        })
                    }

                    dialog.dismiss()
                    pref.setValues("proses", "0")
                    Toast.makeText(this, "Kartu Keluarga telah berhasil diunggah", Toast.LENGTH_LONG).show()
                    Glide.with(this)
                        .load(filePath)
                        .apply(RequestOptions.centerCropTransform())
                        .into(bind.ivPhoto)
                    bind.tvNone.visibility = View.GONE
                }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Dibatalkan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        if (pref.getValues("proses").equals("1")) {
            Toast.makeText(this, "Silakan tunggu sampai file berhasil diunggah", Toast.LENGTH_SHORT).show()
        } else {
            if (pref.getValues("action").equals("profile")) {
                startActivity(Intent(this, HomeActivity::class.java))
            } else {
                super.onBackPressed()
            }
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }
}