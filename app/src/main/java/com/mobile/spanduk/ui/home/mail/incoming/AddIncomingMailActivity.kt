package com.mobile.spanduk.ui.home.mail.incoming

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mobile.spanduk.databinding.ActivityAddIncomingMailBinding
import com.mobile.spanduk.model.Mail
import com.mobile.spanduk.utils.Connection
import com.mobile.spanduk.utils.Preferences
import java.util.*

class AddIncomingMailActivity : AppCompatActivity() {

    lateinit var net: Connection
    lateinit var pref: Preferences
    private lateinit var bind: ActivityAddIncomingMailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityAddIncomingMailBinding.inflate(layoutInflater)
        setContentView(bind.root)

        net = Connection(this)
        pref = Preferences(this)

        if (pref.getValues("action").equals("edit")) {
            bind.mailNum.setText(pref.getValues("no"))
            bind.dateReceived.text = pref.getValues("tgl")
            bind.mailingDate.text = pref.getValues("date")
            bind.from.setText(pref.getValues("from"))
            bind.subject.setText(pref.getValues("subject"))
            bind.detail.setText(pref.getValues("detail"))
            bind.etc.setText(pref.getValues("etc"))
        }

        bind.btnBack.setOnClickListener {
            onBackPressed()
        }

        bind.dateReceived.setOnClickListener {
            pickDate(bind.dateReceived)
        }

        bind.mailingDate.setOnClickListener {
            pickDate(bind.mailingDate)
        }

        bind.btnSubmit.setOnClickListener {
            saveData()
        }
    }

    private fun pickDate(date: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, mYear, mMonth, mDay ->
            val textDate = "$mDay-${mMonth + 1}-$mYear"
            date.text = textDate
        }, year, month, day)
        datePicker.show()
    }

    private fun saveData() {
        val number = bind.mailNum.text.toString().trim()
        val tgl = bind.dateReceived.text.toString().trim()
        val date = bind.mailingDate.text.toString().trim()
        val from = bind.from.text.toString()
        val subject = bind.subject.text.toString()
        val detail = bind.detail.text.toString()
        val etc = bind.etc.text.toString()

        if (number == "") {
            bind.mailNum.error = "Lengkapi isian terlebih dahulu!"
            bind.mailNum.requestFocus()
        } else if (from == "") {
            bind.from.error = "Lengkapi isian terlebih dahulu!"
            bind.from.requestFocus()
        } else if (subject == "") {
            bind.subject.error = "Lengkapi isian terlebih dahulu!"
            bind.subject.requestFocus()
        } else if (detail == "") {
            bind.detail.error = "Lengkapi isian terlebih dahulu!"
            bind.detail.requestFocus()
        } else if (etc == "") {
            bind.etc.error = "Lengkapi isian terlebih dahulu!"
            bind.etc.requestFocus()
        } else if (tgl == "") {
            Toast.makeText(this, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_SHORT).show()
        } else if (date == "") {
            Toast.makeText(this, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_SHORT).show()
        } else {
            if (net.isOnline()) {
                val ref = FirebaseDatabase.getInstance().getReference("Surat Masuk").child(number)
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val data = Mail()
                        data.no = number
                        data.dateReceived = tgl
                        data.mailingDate = date
                        data.from = from
                        data.subject = subject
                        data.detail = detail
                        data.etc = etc

                        ref.setValue(data)
                        Toast.makeText(this@AddIncomingMailActivity, "Data telah diperbarui", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@AddIncomingMailActivity, IncomingMailActivity::class.java))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@AddIncomingMailActivity, ""+error.message, Toast.LENGTH_SHORT).show()
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