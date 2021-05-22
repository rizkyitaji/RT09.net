package com.mobile.spanduk.ui.home.list

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.*
import com.mobile.spanduk.R
import com.mobile.spanduk.databinding.ActivityDaftarWargaBinding
import com.mobile.spanduk.model.Warga
import com.mobile.spanduk.ui.home.HomeActivity
import com.mobile.spanduk.ui.home.profile.ProfileActivity
import com.mobile.spanduk.utils.*
import java.util.*
import kotlin.collections.ArrayList

class DaftarWargaActivity : AppCompatActivity() {

    lateinit var uri: String
    lateinit var name: String
    lateinit var uname: String
    lateinit var pref: Preferences
    lateinit var ref: DatabaseReference
    private var dataList = ArrayList<Warga>()
    private lateinit var bind: ActivityDaftarWargaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityDaftarWargaBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val img = ShowImage(this)
        val net = Connection(this)
        pref = Preferences(this)

        ref = FirebaseDatabase.getInstance().getReference("Warga")

        bind.btnBack.setOnClickListener {
            onBackPressed()
        }

        bind.etSearch.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                bind.ivSearch.setImageResource(R.drawable.ic_close_blue)
                bind.ivSearch.setOnClickListener {
                    bind.etSearch.text.clear()
                }
            }
        }

        val query = FirebaseDatabase.getInstance().getReference("Users")
            .orderByChild("level").equalTo("Ketua RT")
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val id = dataSnapshot.child("username").value.toString()

                    val db = FirebaseDatabase.getInstance().getReference("Users").child(id)
                    db.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            uri = snapshot.child("photo").value.toString()
                            name = snapshot.child("name").value.toString()
                            uname = snapshot.child("username").value.toString()
                            bind.tvName.text = name

                            if (!uri.equals("null")) {
                                Glide.with(this@DaftarWargaActivity)
                                    .load(uri)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(bind.ivProfile)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@DaftarWargaActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DaftarWargaActivity, "" + error.message, Toast.LENGTH_SHORT).show()
            }
        })

        bind.ivProfile.setOnClickListener {
            img.show(uri, "blue")
        }

        bind.btnView.setOnClickListener {
            pref.setValues("uri", uri)
            pref.setValues("name", name)
            pref.setValues("uname", uname)
            pref.setValues("user", "Ketua RT")
            pref.setValues("action", "visit")
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        if (net.isOnline()) {
            val mDatabase = ref.orderByChild("name")

            mDatabase.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    dataList.clear()
                    for (dataSnapshot in snapshot.children) {
                        dataList.add(dataSnapshot.getValue(Warga::class.java)!!)
                    }
                    showData()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DaftarWargaActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                }
            })

            bind.etSearch.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    mDatabase.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            dataList.clear()
                            for (dataSnapshot in snapshot.children) {
                                val item = dataSnapshot.getValue(Warga::class.java)!!
                                val searchName = item.name!!.toLowerCase(Locale.ROOT)
                                val searchAddress = item.address!!.toLowerCase(Locale.ROOT)
                                if (searchName.contains(s.toString().toLowerCase(Locale.ROOT)) ||
                                    searchAddress.contains(s.toString().toLowerCase(Locale.ROOT))) {
                                    bind.cvUserRT.visibility = View.GONE
                                    dataList.add(item)
                                }
                            }
                            showData()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@DaftarWargaActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        } else {
            net.isOffline()
        }

        if (!pref.getValues("level").equals("Admin")) {
            bind.btnAdd.visibility = View.GONE
        }

        bind.btnAdd.setOnClickListener {
            startActivity(Intent(this, AddWargaActivity::class.java)
                    .putExtra("action", "add")
            )
        }
    }

    private fun showData() {
        bind.recyclerView.layoutManager = LinearLayoutManager(this)
        bind.recyclerView.addItemDecoration(DividerItemDecorator(
                ContextCompat.getDrawable(this, R.drawable.divider)
            )
        )
        bind.recyclerView.adapter = DaftarWargaAdapter(dataList) {
            if (pref.getValues("level").equals("Admin")) {
                moreAction()
            } else if (pref.getValues("level").equals("Ketua RT")) {
                startActivity(Intent(this, DetailWargaActivity::class.java))
            }
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
            startActivity(Intent(this, AddWargaActivity::class.java)
                    .putExtra("action", "edit")
            )
        }

        val detail = view.findViewById<LinearLayout>(R.id.view)
        detail.setOnClickListener {
            dialog.menu().dismiss()
            startActivity(Intent(this, DetailWargaActivity::class.java))
        }

        val delete = view.findViewById<LinearLayout>(R.id.delete)
        delete.setOnClickListener {
            ref.child(pref.getValues("nik").toString()).removeValue()
            Toast.makeText(this, "Data telah dihapus", Toast.LENGTH_SHORT).show()
            dialog.menu().dismiss()
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, HomeActivity::class.java))
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}