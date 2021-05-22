package com.mobile.spanduk.ui.home.setting

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mobile.spanduk.R
import com.mobile.spanduk.databinding.ActivitySettingBinding
import com.mobile.spanduk.ui.home.HomeActivity
import com.mobile.spanduk.ui.home.profile.ProfileActivity
import com.mobile.spanduk.ui.sign.SignInActivity
import com.mobile.spanduk.utils.*
import dmax.dialog.SpotsDialog

class SettingActivity : AppCompatActivity() {

    lateinit var wa: String
    lateinit var uri: String
    lateinit var phone: String
    lateinit var net: Connection
    lateinit var pref: Preferences
    lateinit var ref: DatabaseReference
    lateinit var imgRef: StorageReference
    private lateinit var bind: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(bind.root)

        net = Connection(this)
        pref = Preferences(this)
        val img = ShowImage(this)

        val id = pref.getValues("username").toString()
        val name = pref.getValues("nameLogin").toString()
        val level = pref.getValues("level").toString()
        val mDatabase = FirebaseDatabase.getInstance().getReference("Users")

        ref = mDatabase.child(id)
        imgRef = FirebaseStorage.getInstance().getReference("images/$id")

        bind.btnBack.setOnClickListener {
            onBackPressed()
        }

        if (net.isOnline()) {
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    uri = snapshot.child("photo").value.toString()

                    if (!uri.equals("null")) {
                        Glide.with(this@SettingActivity)
                            .load(uri)
                            .apply(RequestOptions.circleCropTransform())
                            .into(bind.ivProfile)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SettingActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            })

            bind.tvName.text = name
            bind.tvLevel.text = level
        } else {
            net.isOffline()
        }

        bind.ivProfile.setOnClickListener {
            img.show(uri, "white")
        }

        bind.ivAdd.setOnClickListener {
            val dialog = BottomSheetDialog(this)
            val view = dialog.build(R.layout.layout_upload_photo)

            val close = view.findViewById<ImageView>(R.id.iv_close)
            close.setOnClickListener {
                dialog.menu().dismiss()
            }

            val camera = view.findViewById<LinearLayout>(R.id.camera)
            camera.setOnClickListener {
                if (net.isOnline()) {
                    ImagePicker.with(this)
                        .cameraOnly()
                        .cropSquare()
                        .start()
                    dialog.menu().dismiss()
                } else {
                    net.isOffline()
                }
            }

            val gallery = view.findViewById<LinearLayout>(R.id.gallery)
            gallery.setOnClickListener {
                if (net.isOnline()) {
                    ImagePicker.with(this)
                        .galleryOnly()
                        .cropSquare()
                        .start()
                    dialog.menu().dismiss()
                } else {
                    net.isOffline()
                }
            }

            val delete = view.findViewById<LinearLayout>(R.id.delete)
            delete.setOnClickListener {
                if (net.isOnline()) {
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val photo = snapshot.child("photo").value.toString()
                            if (!photo.equals("null")) {
                                ref.child("photo").removeValue()
                                bind.ivProfile.setImageResource(R.drawable.ic_account_circle_white)

                                imgRef.delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(this@SettingActivity, "Foto telah dihapus", Toast.LENGTH_SHORT).show()
                                    }

                                    .addOnFailureListener {
                                        Toast.makeText(this@SettingActivity, "Error", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(this@SettingActivity, "Anda belum mengunggah foto", Toast.LENGTH_SHORT).show()
                            }
                            dialog.menu().dismiss()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@SettingActivity, error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    net.isOffline()
                }
            }
        }

        bind.btnPassword.setOnClickListener {
            editData("password")
        }

        bind.btnPhone.setOnClickListener {
            editData("phone")
        }

        bind.btnWhatsapp.setOnClickListener {
            editData("whatsapp")
        }

        bind.btnContact.setOnClickListener {
            if (net.isOnline()) {
                if (bind.expandableView.visibility == View.GONE) {
                    TransitionManager.beginDelayedTransition(bind.layoutOther, AutoTransition())
                    bind.expandableView.visibility = View.VISIBLE
                } else {
                    TransitionManager.beginDelayedTransition(bind.layoutOther, AutoTransition())
                    bind.expandableView.visibility = View.GONE
                }
            } else {
                net.isOffline()
            }
        }

        val query = mDatabase.orderByChild("level").equalTo("Ketua RT")
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val username = dataSnapshot.child("username").value.toString()

                    mDatabase.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            phone = snapshot.child("phone").value.toString()
                            wa = snapshot.child("whatsapp").value.toString()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@SettingActivity, error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SettingActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })

        bind.phone.setOnClickListener {
            if (net.isOnline()) {
                if (!phone.equals("null")) {
                    phoneCall()
                } else {
                    Toast.makeText(this, "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT).show()
                }
            } else {
                net.isOffline()
            }
        }

        bind.whatsapp.setOnClickListener {
            if (net.isOnline()) {
                if (!wa.equals("null")) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/=62" + wa)))
                } else {
                    Toast.makeText(this, "Nomor whatsapp tidak tersedia", Toast.LENGTH_SHORT).show()
                }
            } else {
                net.isOffline()
            }
        }

        bind.btnLogout.setOnClickListener {
            finishAffinity()
            pref.setValues("status", "0")
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }

    private fun editData(path: String) {
        val dialog = AlertDialog(this)
        val view = dialog.build(R.layout.layout_edit_profile)
        dialog.menu().setCancelable(false)

        val close = view.findViewById<ImageView>(R.id.iv_close)
        close.setOnClickListener {
            dialog.menu().dismiss()
        }

        val title = view.findViewById<TextView>(R.id.tv_title)
        val editCurrent = view.findViewById<TextView>(R.id.tv_current)
        val textCurrent = view.findViewById<TextView>(R.id.textCurrent)
        val textNew = view.findViewById<TextView>(R.id.textNew)
        val editNew = view.findViewById<EditText>(R.id.et_new)
        val save = view.findViewById<Button>(R.id.btn_save)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val phone = snapshot.child("phone").value.toString()
                val wa = snapshot.child("whatsapp").value.toString()
                val pass = snapshot.child("password").value.toString()

                if (path.equals("password")) {
                    editCurrent.text = pass
                    editNew.inputType = InputType.TYPE_CLASS_TEXT
                } else if (path.equals("phone")) {
                    title.text = getString(R.string.edit_no_telepon)
                    textCurrent.text = getString(R.string.no_telepon_saat_ini)
                    textNew.text = getString(R.string.no_telepon_baru)
                    if (phone.equals("null")) {
                        editCurrent.text = getString(R.string.phone_not_set)
                    } else {
                        editCurrent.text = phone
                    }
                } else {
                    title.text = getString(R.string.edit_no_whatsapp)
                    textCurrent.text = getString(R.string.no_whatsapp_saat_ini)
                    textNew.text = getString(R.string.no_whatsapp_baru)
                    if (phone.equals("null")) {
                        editCurrent.text = getString(R.string.whatsapp_not_set)
                    } else {
                        val setWA = "0$wa"
                        editCurrent.text = setWA
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SettingActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })

        save.setOnClickListener {
            val new = editNew.text.toString().trim()

            if (new.equals("")) {
                editNew.error = "Lengkapi isian terlebih dahulu!"
                editNew.requestFocus()
            } else {
                if (net.isOnline()) {
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (path.equals("whatsapp")) {
                                val wa = new.substring(1)
                                ref.child(path).setValue(wa)
                            } else {
                                ref.child(path).setValue(new)
                            }
                            dialog.menu().dismiss()
                            Toast.makeText(this@SettingActivity, "Data telah diperbarui", Toast.LENGTH_SHORT).show()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@SettingActivity, error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    net.isOffline()
                }
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
                        ref.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                ref.child("photo").setValue(it.toString())
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@SettingActivity, error.message, Toast.LENGTH_SHORT).show()
                            }
                        })
                    }

                    dialog.dismiss()
                    pref.setValues("proses", "0")
                    Toast.makeText(this, "Foto profil telah diperbarui", Toast.LENGTH_LONG).show()
                }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Dibatalkan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        if (pref.getValues("proses").equals("1")) {
            Toast.makeText(this, "Silakan tunggu sampai foto berhasil diunggah", Toast.LENGTH_SHORT).show()
        } else {
            pref.setValues("photo", uri)
            if (pref.getValues("action").equals("profile")) {
                startActivity(Intent(this, ProfileActivity::class.java))
            } else {
                startActivity(Intent(this, HomeActivity::class.java))
            }
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }
}