package com.mobile.spanduk.ui.sign

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.*
import com.mobile.spanduk.R
import com.mobile.spanduk.databinding.ActivitySignInBinding
import com.mobile.spanduk.model.Users
import com.mobile.spanduk.ui.home.HomeActivity
import com.mobile.spanduk.utils.AlertDialog
import com.mobile.spanduk.utils.Connection
import com.mobile.spanduk.utils.Preferences

class SignInActivity : AppCompatActivity() {

    lateinit var net: Connection
    lateinit var pref: Preferences
    lateinit var ref: DatabaseReference
    private lateinit var bind: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(bind.root)

        net = Connection(this)
        pref = Preferences(this)

        ref = FirebaseDatabase.getInstance().getReference("Users")

        if (pref.getValues("status").equals("1")) {
            pref.setValues("tap", "0")
            startActivity(Intent(this@SignInActivity, HomeActivity::class.java))
        }

        bind.btnOption.setOnClickListener {
            optionUser()
        }

        checkLogin()

        bind.btnForgotPass.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun optionUser() {
        val dialog = AlertDialog(this)
        val view = dialog.build(R.layout.layout_option_user)

        val warga = view.findViewById<TextView>(R.id.tv_warga)
        warga.setOnClickListener {
            dialog.menu().dismiss()
            val level = getString(R.string.warga)
            bind.textUser.text = level
            checkLogin()
        }

        val rt = view.findViewById<TextView>(R.id.tv_rt)
        rt.setOnClickListener {
            dialog.menu().dismiss()
            val level = getString(R.string.ketua_rt)
            bind.textUser.text = level
            checkLogin()
        }

        val admin = view.findViewById<TextView>(R.id.tv_admin)
        admin.setOnClickListener {
            dialog.menu().dismiss()
            val level = getString(R.string.admin)
            bind.textUser.text = level
            checkLogin()
        }
    }

    private fun checkLogin() {
        bind.btnLogin.setOnClickListener {
            val id = bind.editID.text.toString().trim()
            val pass = bind.editPass.text.toString().trim()
            val user = bind.textUser.text.toString()

            if (id.equals("")) {
                bind.editID.error = "Lengkapi isian terlebih dahulu!"
                bind.editID.requestFocus()
            } else if (pass.equals("")) {
                bind.editPass.error = "Lengkapi isian terlebih dahulu!"
                bind.editPass.requestFocus()
            } else {
                if (net.isOnline()) {
                    if (user.equals("SELECT USER")) {
                        Toast.makeText(this, "Silakan pilih jenis user terlebih dahulu!", Toast.LENGTH_SHORT).show()
                    } else {
                        gotoLogin(id, pass, user)
                    }
                } else {
                    net.isOffline()
                }
            }
        }
    }

    private fun gotoLogin(id: String, pass: String, level: String) {
        ref.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.getValue(Users::class.java)
                if (users == null) {
                    checkLogin()
                    Toast.makeText(this@SignInActivity, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
                    bind.btnOption.setOnClickListener {
                        optionUser()
                    }
                } else {
                    if (users.level.equals(level)) {
                        if (users.password.equals(pass)) {
                            val uri = snapshot.child("photo").value.toString()
                            val name = snapshot.child("name").value.toString()

                            pref.setValues("tap", "0")
                            pref.setValues("photo", uri)
                            pref.setValues("level", level)
                            pref.setValues("username", id)
                            pref.setValues("nameLogin", name)
                            pref.setValues("status", "1")
                            Toast.makeText(this@SignInActivity, "Selamat Datang", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@SignInActivity, HomeActivity::class.java))
                        } else {
                            Toast.makeText(this@SignInActivity, "Password salah!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@SignInActivity, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SignInActivity, "" + error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}