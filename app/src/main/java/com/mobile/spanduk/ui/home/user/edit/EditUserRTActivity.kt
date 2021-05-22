package com.mobile.spanduk.ui.home.user.edit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mobile.spanduk.databinding.ActivityEditUserRtBinding
import com.mobile.spanduk.model.Users
import com.mobile.spanduk.ui.home.user.UserActivity
import com.mobile.spanduk.ui.home.user.UserAdapter
import com.mobile.spanduk.utils.Connection
import com.mobile.spanduk.utils.Preferences

class EditUserRTActivity : AppCompatActivity() {

    private var dataList = ArrayList<Users>()
    private lateinit var bind: ActivityEditUserRtBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityEditUserRtBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val net = Connection(this)
        val pref = Preferences(this)

        pref.setValues("page", "edit")
        val ref = FirebaseDatabase.getInstance().getReference("Users")

        if (net.isOnline()) {
            val mDatabase = ref.orderByChild("level").equalTo("Warga")

            mDatabase.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    dataList.clear()
                    for (dataSnapshot in snapshot.children) {
                        dataList.add(dataSnapshot.getValue(Users::class.java)!!)
                    }
                    bind.recyclerView.layoutManager = LinearLayoutManager(this@EditUserRTActivity)
                    bind.recyclerView.adapter = UserAdapter(dataList){}
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EditUserRTActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            net.isOffline()
        }

        bind.btnSelect.setOnClickListener {
            val lastDB = ref.child(pref.getValues("uname").toString())
            lastDB.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lastDB.child("level").setValue("Warga")
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EditUserRTActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            })

            val newDB = ref.child(pref.getValues("id").toString())
            newDB.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    newDB.child("level").setValue("Ketua RT")
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EditUserRTActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            })

            Toast.makeText(this, "Data telah diperbarui", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, UserActivity::class.java))
        }
    }
}