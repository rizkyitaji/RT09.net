package com.mobile.spanduk.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mobile.spanduk.R
import com.mobile.spanduk.ui.sign.SignInActivity
import com.mobile.spanduk.utils.Connection
import com.mobile.spanduk.utils.Preferences

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val net = Connection(this)
        val pref = Preferences(this)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if (net.isOnline()) {
                val id = pref.getValues("username").toString()
                val ref = FirebaseDatabase.getInstance().getReference("Users")
                ref.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val uri = snapshot.child("photo").value.toString()
                        val name = snapshot.child("name").value.toString()
                        val level = snapshot.child("level").value.toString()

//                        if (!level.equals("Admin")) {
//                            if (name.equals("null")) {
//                                pref.setValues("status", "0")
//                                Toast.makeText(this@SplashScreenActivity,
//                                    "Maaf akun Anda tidak ada atau sudah dihapus",
//                                    Toast.LENGTH_SHORT).show()
//                            } else {
                                pref.setValues("photo", uri)
                                pref.setValues("level", level)
                                pref.setValues("nameLogin", name)
//                            }
//                        }
                        startActivity(Intent(this@SplashScreenActivity, SignInActivity::class.java))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@SplashScreenActivity, error.message, Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                net.isOffline()
            }
        }, 1000)
    }
}