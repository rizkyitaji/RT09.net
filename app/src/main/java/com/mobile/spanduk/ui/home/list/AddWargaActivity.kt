package com.mobile.spanduk.ui.home.list

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mobile.spanduk.R
import com.mobile.spanduk.databinding.ActivityAddWargaBinding
import com.mobile.spanduk.model.Warga
import com.mobile.spanduk.utils.AlertDialog
import com.mobile.spanduk.utils.Connection
import com.mobile.spanduk.utils.Preferences

class AddWargaActivity : AppCompatActivity() {

    lateinit var pref: Preferences
    private lateinit var bind: ActivityAddWargaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityAddWargaBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val net = Connection(this)
        pref = Preferences(this)

        val action = intent.getStringExtra("action")
        if (action.equals("edit")) {
            bind.etNik.setText(pref.getValues("nik"))
            bind.etName.setText(pref.getValues("name"))
            bind.etTtl.setText(pref.getValues("ttl"))
            bind.tvGender.setText(pref.getValues("gender"))
            bind.etKk.setText(pref.getValues("kk"))
            bind.tvReligion.setText(pref.getValues("religion"))
            bind.etAddress.setText(pref.getValues("address"))
            bind.etEducation.setText(pref.getValues("education"))
            bind.etJob.setText(pref.getValues("job"))
            bind.etPhone.setText(pref.getValues("phone"))
        }

        bind.btnBack.setOnClickListener {
            onBackPressed()
        }

        bind.btnGender.setOnClickListener {
            val dialog = AlertDialog(this)
            val view = dialog.build(R.layout.layout_gender)

            val male = view.findViewById<TextView>(R.id.tv_male)
            male.setOnClickListener {
                dialog.menu().dismiss()
                bind.tvGender.text = getString(R.string.laki_laki)
            }

            val female = view.findViewById<TextView>(R.id.tv_female)
            female.setOnClickListener {
                dialog.menu().dismiss()
                bind.tvGender.text = getString(R.string.perempuan)
            }
        }

        bind.btnReligion.setOnClickListener {
            val dialog = AlertDialog(this)
            val view = dialog.build(R.layout.layout_religion)

            val islam = view.findViewById<TextView>(R.id.tv_islam)
            islam.setOnClickListener {
                dialog.menu().dismiss()
                bind.tvReligion.text = getString(R.string.islam)
            }

            val protestan = view.findViewById<TextView>(R.id.tv_protestan)
            protestan.setOnClickListener {
                dialog.menu().dismiss()
                bind.tvReligion.text = getString(R.string.protestan)
            }

            val katolik = view.findViewById<TextView>(R.id.tv_katolik)
            katolik.setOnClickListener {
                dialog.menu().dismiss()
                bind.tvReligion.text = getString(R.string.katolik)
            }

            val hindu = view.findViewById<TextView>(R.id.tv_hindu)
            hindu.setOnClickListener {
                dialog.menu().dismiss()
                bind.tvReligion.text = getString(R.string.hindu)
            }

            val buddha = view.findViewById<TextView>(R.id.tv_buddha)
            buddha.setOnClickListener {
                dialog.menu().dismiss()
                bind.tvReligion.text = getString(R.string.buddha)
            }

            val khonghucu = view.findViewById<TextView>(R.id.tv_khonghucu)
            khonghucu.setOnClickListener {
                dialog.menu().dismiss()
                bind.tvReligion.text = getString(R.string.khonghucu)
            }
        }

        bind.btnSubmit.setOnClickListener {
            val nik = bind.etNik.text.toString().trim()
            val name = bind.etName.text.toString()
            val ttl = bind.etTtl.text.toString()
            val gender = bind.tvGender.text.toString()
            val kk = bind.etKk.text.toString().trim()
            val religion = bind.tvReligion.text.toString()
            val address = bind.etAddress.text.toString()
            val education = bind.etEducation.text.toString()
            val job = bind.etJob.text.toString()
            val phone = bind.etPhone.text.toString().trim()

            if (nik == "") {
                bind.etNik.error = "Lengkapi isian terlebih dahulu!"
                bind.etNik.requestFocus()
            } else if (name == "") {
                bind.etName.error = "Lengkapi isian terlebih dahulu!"
                bind.etName.requestFocus()
            } else if (ttl == "") {
                bind.etTtl.error = "Lengkapi isian terlebih dahulu!"
                bind.etTtl.requestFocus()
            } else if (kk == "") {
                bind.etKk.error = "Lengkapi isian terlebih dahulu!"
                bind.etKk.requestFocus()
            } else if (address == "") {
                bind.etAddress.error = "Lengkapi isian terlebih dahulu!"
                bind.etAddress.requestFocus()
            } else if (education == "") {
                bind.etEducation.error = "Lengkapi isian terlebih dahulu!"
                bind.etEducation.requestFocus()
            } else if (job == "") {
                bind.etJob.error = "Lengkapi isian terlebih dahulu!"
                bind.etJob.requestFocus()
            } else if (phone == "") {
                bind.etPhone.error = "Lengkapi isian terlebih dahulu!"
                bind.etPhone.requestFocus()
            } else if (gender == "PILIH") {
                Toast.makeText(this, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_SHORT).show()
            } else if (religion == "PILIH") {
                Toast.makeText(this, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_SHORT).show()
            } else {
                if (net.isOnline()) {
                    val ref = FirebaseDatabase.getInstance().getReference("Warga").child(nik)
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val data = Warga()

                            data.kk = kk
                            data.nik = nik
                            data.ttl = ttl
                            data.job = job
                            data.name = name
                            data.phone = phone
                            data.gender = gender
                            data.address = address
                            data.religion = religion
                            data.education = education

                            ref.setValue(data)
                            startActivity(Intent(this@AddWargaActivity, DaftarWargaActivity::class.java))
                            Toast.makeText(this@AddWargaActivity, "Data telah diperbarui", Toast.LENGTH_SHORT).show()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@AddWargaActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    net.isOffline()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (pref.getValues("tap").equals("0")) {
            pref.setValues("tap", "1")
            Toast.makeText(this, "Tap again to exit", Toast.LENGTH_SHORT).show()
        } else {
            pref.setValues("tap", "0")
            super.onBackPressed()
        }
    }
}