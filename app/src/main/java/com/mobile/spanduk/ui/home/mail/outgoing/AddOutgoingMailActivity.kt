package com.mobile.spanduk.ui.home.mail.outgoing

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.*
import com.mobile.spanduk.R
import com.mobile.spanduk.databinding.ActivityAddOutgoingMailBinding
import com.mobile.spanduk.model.Surat
import com.mobile.spanduk.ui.home.mail.MailActivity
import com.mobile.spanduk.utils.AlertDialog
import com.mobile.spanduk.utils.Connection
import com.mobile.spanduk.utils.Preferences
import java.text.SimpleDateFormat
import java.util.*

class AddOutgoingMailActivity : AppCompatActivity() {

    lateinit var id: String
    lateinit var getName: String
    lateinit var net: Connection
    lateinit var pref: Preferences
    lateinit var mDatabase: DatabaseReference
    private lateinit var bind: ActivityAddOutgoingMailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityAddOutgoingMailBinding.inflate(layoutInflater)
        setContentView(bind.root)

        net = Connection(this)
        pref = Preferences(this)

        mDatabase = FirebaseDatabase.getInstance().getReference("Surat Keluar")

        getName = pref.getValues("nameLogin").toString()

        if (pref.getValues("action").equals("edit")) {
            bind.etNik.setText(pref.getValues("nik"))
            bind.etName.setText(pref.getValues("name"))
            bind.etTtl.setText(pref.getValues("ttl"))
            bind.tvGender.text = pref.getValues("gender")
            bind.tvReligion.text = pref.getValues("religion")
            bind.etAddress.setText(pref.getValues("address"))
            bind.etJob.setText(pref.getValues("job"))
            bind.tvDate.text = pref.getValues("date")
            bind.etSubject.setText(pref.getValues("subject"))

            id = pref.getValues("id").toString()
        } else {
            if (pref.getValues("level").equals("Warga")) {
                bind.etName.setText(getName)
            }
            id = mDatabase.push().key.toString()
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

        bind.tvDate.setOnClickListener {
            pickDate()
        }

        bind.btnSubmit.setOnClickListener {
            saveData()
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

    private fun saveData() {
        val nik = bind.etNik.text.toString().trim()
        val name = bind.etName.text.toString()
        val ttl = bind.etTtl.text.toString()
        val gender = bind.tvGender.text.toString()
        val religion = bind.tvReligion.text.toString()
        val address = bind.etAddress.text.toString()
        val job = bind.etJob.text.toString()
        val date = bind.tvDate.text.toString().trim()
        val subject = bind.etSubject.text.toString()

        if (nik == "") {
            bind.etNik.error = "Lengkapi isian terlebih dahulu!"
            bind.etNik.requestFocus()
        } else if (name == "") {
            bind.etName.error = "Lengkapi isian terlebih dahulu!"
            bind.etName.requestFocus()
        } else if (ttl == "") {
            bind.etTtl.error = "Lengkapi isian terlebih dahulu!"
            bind.etTtl.requestFocus()
        } else if (address == "") {
            bind.etAddress.error = "Lengkapi isian terlebih dahulu!"
            bind.etAddress.requestFocus()
        } else if (job == "") {
            bind.etJob.error = "Lengkapi isian terlebih dahulu!"
            bind.etJob.requestFocus()
        } else if (subject == "") {
            bind.etSubject.error = "Lengkapi isian terlebih dahulu!"
            bind.etSubject.requestFocus()
        } else if (gender == "PILIH") {
            Toast.makeText(this, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_SHORT).show()
        } else if (religion == "PILIH") {
            Toast.makeText(this, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_SHORT).show()
        } else if (date == "PILIH TANGGAL") {
            Toast.makeText(this, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_SHORT).show()
        } else {
            if (net.isOnline()) {
                val ref = mDatabase.child(id)
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val data = Surat()
                        data.id = id
                        data.nik = nik
                        data.name = name
                        data.sort = getName
                        data.ttl = ttl
                        data.gender = gender
                        data.religion = religion
                        data.address = address
                        data.job = job
                        data.date = date
                        data.subject = subject

                        if (pref.getValues("action").equals("add")) {
                            data.status = "Menunggu"
                            data.tgl = SimpleDateFormat("d-M-yyyy", Locale.ENGLISH).format(Date())
                        }

                        ref.setValue(data)
                        Toast.makeText(this@AddOutgoingMailActivity, "Data telah diperbarui", Toast.LENGTH_SHORT).show()
                        if (pref.getValues("mail").equals("warga")) {
                            startActivity(Intent(this@AddOutgoingMailActivity, MailActivity::class.java))
                        } else {
                            startActivity(Intent(this@AddOutgoingMailActivity, OutgoingMailActivity::class.java))
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@AddOutgoingMailActivity, error.message, Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                net.isOffline()
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