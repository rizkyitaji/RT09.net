package com.mobile.spanduk.ui.sign

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mobile.spanduk.R
import com.mobile.spanduk.databinding.ActivityForgotPasswordBinding
import com.mobile.spanduk.utils.Connection
import com.mobile.spanduk.utils.Preferences

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var bind: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val net = Connection(this)
        val pref = Preferences(this)

        bind.btnBack.setOnClickListener {
            onBackPressed()
        }

        bind.btnContinue.setOnClickListener {
            val phone = bind.etPhone.text.toString().trim()

            if (phone == "") {
                bind.etPhone.error = "Lengkapi isian terlebih dahulu!"
                bind.etPhone.requestFocus()
            } else {
                if (net.isOnline()) {
                    val ref = FirebaseDatabase.getInstance().getReference("Users")
                        .orderByChild("phone").equalTo(phone)
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (dataSnapshot in snapshot.children) {
                                val username = dataSnapshot.child("username").value.toString()
                                val setPhone = "+62" + phone.substring(1)

                                if (snapshot.exists()) {
                                    pref.setValues("phone", setPhone)
                                    pref.setValues("username", username)
                                    startActivity(Intent(this@ForgotPasswordActivity, VerifyOTPActivity::class.java))
                                } else {
                                    Toast.makeText(this@ForgotPasswordActivity, "Nomor telepon tidak terdaftar", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@ForgotPasswordActivity, error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    net.isOffline()
                }
            }
        }
    }
}