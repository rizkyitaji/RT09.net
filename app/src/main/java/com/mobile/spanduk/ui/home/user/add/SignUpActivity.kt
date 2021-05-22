package com.mobile.spanduk.ui.home.user.add

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.database.*
import com.mobile.spanduk.databinding.ActivitySignUpBinding
import com.mobile.spanduk.model.Users
import com.mobile.spanduk.ui.home.user.UserActivity
import com.mobile.spanduk.utils.Connection
import com.mobile.spanduk.utils.Preferences

class SignUpActivity : AppCompatActivity() {

    lateinit var id: String
    lateinit var name: String
    lateinit var pass: String
    lateinit var phone: String
    lateinit var action: String
    lateinit var whatsapp: String
    lateinit var net: Connection
    lateinit var pref: Preferences
    private lateinit var bind: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(bind.root)

        net = Connection(this)
        pref = Preferences(this)

        action = intent.getStringExtra("action")!!
        if (action.equals("edit")) {
            bind.etName.setText(pref.getValues("name"))
            bind.etPass.setText(pref.getValues("pass"))
            bind.etPhone.setText(pref.getValues("phone"))
            bind.etUsername.setText(pref.getValues("uname"))
            bind.tvUsername.text = pref.getValues("uname")

            bind.etUsername.visibility = View.GONE
            val wa = "0" + pref.getValues("whatsapp")
            bind.etWhatsapp.setText(wa)
        } else {
            bind.tvUsername.visibility = View.GONE
        }

        bind.btnBack.setOnClickListener {
            onBackPressed()
        }

        saveData()
    }

    private fun saveData() {
        bind.btnSave.setOnClickListener {
            name = bind.etName.text.toString()
            pass = bind.etPass.text.toString().trim()
            id = bind.etUsername.text.toString().trim()
            phone = bind.etPhone.text.toString().trim()
            whatsapp = bind.etWhatsapp.text.toString().trim()

            if (name.equals("")) {
                bind.etName.error = "Lengkapi isian terlebih dahulu!"
                bind.etName.requestFocus()
            } else if (id.equals("")) {
                bind.etUsername.error = "Lengkapi isian terlebih dahulu!"
                bind.etUsername.requestFocus()
            } else if (pass.equals("")) {
                bind.etPass.error = "Lengkapi isian terlebih dahulu!"
                bind.etPass.requestFocus()
            } else if (phone.equals("")) {
                bind.etPhone.error = "Lengkapi isian terlebih dahulu!"
                bind.etPhone.requestFocus()
            } else if (whatsapp.equals("")) {
                bind.etWhatsapp.error = "Lengkapi isian terlebih dahulu!"
                bind.etWhatsapp.requestFocus()
            } else {
                if (net.isOnline()) {
                    if (id.contains(".")) {
                        Toast.makeText(this, "Username tidak boleh mengandung .", Toast.LENGTH_SHORT).show()
                    }

                    val ref = FirebaseDatabase.getInstance().getReference("Users").child(id)
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val user = snapshot.getValue(Users::class.java)

                            if (action.equals("add")) {
                                if (user == null) {
                                    setValue(ref)
                                } else {
                                    Toast.makeText(this@SignUpActivity, "Username $id sudah ada", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                setValue(ref)
                            }

                            startActivity(Intent(this@SignUpActivity, UserActivity::class.java))
                            Toast.makeText(this@SignUpActivity, "Data telah diperbarui", Toast.LENGTH_SHORT).show()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@SignUpActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    net.isOffline()
                }
            }
        }
    }

    private fun setValue(ref: DatabaseReference) {
        val wa = whatsapp.substring(1)

        ref.child("name").setValue(name)
        ref.child("phone").setValue(phone)
        ref.child("whatsapp").setValue(wa)
        ref.child("username").setValue(id)
        ref.child("password").setValue(pass)
        ref.child("level").setValue("Warga")
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