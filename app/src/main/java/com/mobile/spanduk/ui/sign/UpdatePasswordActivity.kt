package com.mobile.spanduk.ui.sign

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mobile.spanduk.databinding.ActivityUpdatePasswordBinding
import com.mobile.spanduk.utils.Connection
import com.mobile.spanduk.utils.Preferences

class UpdatePasswordActivity : AppCompatActivity() {

    private lateinit var bind: ActivityUpdatePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityUpdatePasswordBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val net = Connection(this)
        val pref = Preferences(this)

        val username = pref.getValues("username").toString()

        val hello = "Hai, $username"
        bind.tvUsername.text = hello

        bind.btnBack.setOnClickListener {
            onBackPressed()
        }

        bind.btnUpdate.setOnClickListener {
            val new = bind.etNew.text.toString().trim()
            val confirm = bind.etConfirm.text.toString().trim()

            if (new == "") {
                bind.etNew.error = "Lengkapi isian terlebih dahulu!"
                bind.etNew.requestFocus()
            } else if (confirm == "") {
                bind.etConfirm.error = "Lengkapi isian terlebih dahulu!"
                bind.etConfirm.requestFocus()
            } else if (!new.equals(confirm)) {
                bind.etConfirm.error = "Password tidak sama, silakan input ulang!"
                bind.etConfirm.requestFocus()
            } else {
                if (net.isOnline()) {
                    val ref = FirebaseDatabase.getInstance().getReference("Users").child(username)
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            ref.child("password").setValue(confirm)
                            Toast.makeText(this@UpdatePasswordActivity, "Password berhasil diperbarui", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@UpdatePasswordActivity, SignInActivity::class.java))
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@UpdatePasswordActivity, error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    net.isOffline()
                }
            }
        }
    }

    override fun onBackPressed() {
        Toast.makeText(this, "Silakan update password Anda", Toast.LENGTH_SHORT).show()
    }
}