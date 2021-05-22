package com.mobile.spanduk.ui.home.user

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.mobile.spanduk.R
import com.mobile.spanduk.databinding.ActivityUserBinding
import com.mobile.spanduk.model.Users
import com.mobile.spanduk.ui.home.HomeActivity
import com.mobile.spanduk.ui.home.profile.ProfileActivity
import com.mobile.spanduk.ui.home.user.add.SignUpActivity
import com.mobile.spanduk.ui.home.user.edit.EditUserRTActivity
import com.mobile.spanduk.utils.AlertDialog
import com.mobile.spanduk.utils.Connection
import com.mobile.spanduk.utils.Preferences
import java.util.*
import kotlin.collections.ArrayList

class UserActivity : AppCompatActivity() {

    lateinit var id: String
    lateinit var uri: String
    lateinit var name: String
    lateinit var uname: String
    lateinit var pref: Preferences
    lateinit var ref: DatabaseReference
    private var dataList = ArrayList<Users>()
    private lateinit var bind: ActivityUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityUserBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val net = Connection(this)
        pref = Preferences(this)

        pref.setValues("page", "list")

        bind.btnBack.setOnClickListener {
            onBackPressed()
        }

        bind.etSearch.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                bind.cvUserRT.visibility = View.VISIBLE
                bind.ivSearch.setImageResource(R.drawable.ic_close_blue)
                bind.ivSearch.setOnClickListener {
                    bind.etSearch.text.clear()
                }
            }
        }

        ref = FirebaseDatabase.getInstance().getReference("Users")

        val query = ref.orderByChild("level").equalTo("Ketua RT")
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    id = dataSnapshot.child("username").value.toString()

                    val db = ref.child(id)
                    db.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            uri = snapshot.child("photo").value.toString()
                            name = snapshot.child("name").value.toString()
                            uname = snapshot.child("username").value.toString()
                            bind.tvName.text = name

                            if (!uri.equals("null")) {
                                Glide.with(this@UserActivity)
                                    .load(uri)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(bind.ivProfile)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@UserActivity, error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })

        bind.ivProfile.setOnClickListener {
            pref.setValues("uri", uri)
            pref.setValues("name", name)
            pref.setValues("uname", uname)
            pref.setValues("user", "Ketua RT")
            pref.setValues("action", "visit")
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        bind.btnChange.setOnClickListener {
            startActivity(Intent(this, EditUserRTActivity::class.java))
        }

        if (net.isOnline()) {
            val mDatabase = ref.orderByChild("level").equalTo("Warga")

            mDatabase.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    dataList.clear()
                    for (dataSnapshot in snapshot.children) {
                        dataList.add(dataSnapshot.getValue(Users::class.java)!!)
                    }
                    showData()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@UserActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            })

            bind.etSearch.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    mDatabase.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            dataList.clear()
                            for (dataSnapshot in snapshot.children) {
                                val item = dataSnapshot.getValue(Users::class.java)!!
                                val reference = item.name!!.toLowerCase(Locale.ROOT)
                                if (reference.contains(s.toString().toLowerCase(Locale.ROOT))) {
                                    bind.cvUserRT.visibility = View.GONE
                                    dataList.add(item)
                                } else if (reference.equals(null)) {
                                    bind.cvUserRT.visibility = View.VISIBLE
                                }
                            }
                            showData()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@UserActivity, error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        } else {
            net.isOffline()
        }

        bind.btnAdd.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java)
                .putExtra("action", "add"))
        }
    }

    private fun showData() {
        bind.recyclerView.layoutManager = LinearLayoutManager(this)
        bind.recyclerView.adapter = UserAdapter(dataList){
            moreAction()
        }
    }

    private fun moreAction() {
        val dialog = AlertDialog(this)
        val view = dialog.build(R.layout.layout_action)

        val close = view.findViewById<ImageView>(R.id.iv_close)
        close.setOnClickListener {
            dialog.menu().dismiss()
        }

        val edit = view.findViewById<LinearLayout>(R.id.edit)
        edit.setOnClickListener {
            dialog.menu().dismiss()
            startActivity(Intent(this, SignUpActivity::class.java)
                .putExtra("action", "edit"))
        }

        val detail = view.findViewById<LinearLayout>(R.id.view)
        detail.setOnClickListener {
            dialog.menu().dismiss()
            startActivity(Intent(this, DetailUserActivity::class.java))
        }

        val delete = view.findViewById<LinearLayout>(R.id.delete)
        delete.setOnClickListener {
            val id = pref.getValues("uname").toString()
            ref.child(id).removeValue()

            val imgRef  = FirebaseStorage.getInstance().getReference("images/$id")
            imgRef.delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Data telah dihapus", Toast.LENGTH_SHORT).show()
                    dialog.menu().dismiss()
                }

                .addOnFailureListener {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, HomeActivity::class.java))
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}