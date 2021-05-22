package com.mobile.spanduk.ui.home.mail

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
import com.google.firebase.database.*
import com.mobile.spanduk.R
import com.mobile.spanduk.databinding.ActivityMailBinding
import com.mobile.spanduk.model.Surat
import com.mobile.spanduk.ui.home.HomeActivity
import com.mobile.spanduk.ui.home.mail.incoming.IncomingMailActivity
import com.mobile.spanduk.ui.home.mail.outgoing.AddOutgoingMailActivity
import com.mobile.spanduk.ui.home.mail.outgoing.DetailOutgoingMailActivity
import com.mobile.spanduk.ui.home.mail.outgoing.OutgoingMailActivity
import com.mobile.spanduk.ui.home.mail.outgoing.OutgoingMailAdapter
import com.mobile.spanduk.utils.AlertDialog
import com.mobile.spanduk.utils.Connection
import com.mobile.spanduk.utils.Preferences
import java.util.*

class MailActivity : AppCompatActivity() {

    lateinit var pref: Preferences
    lateinit var mDatabase: DatabaseReference
    private var dataList = ArrayList<Surat>()
    private lateinit var bind: ActivityMailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMailBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val net = Connection(this)
        pref = Preferences(this)

        pref.setValues("mail", "warga")
        val name = pref.getValues("nameLogin").toString()

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

        mDatabase = FirebaseDatabase.getInstance().getReference("Surat Keluar")

        if (net.isOnline()) {
            mDatabase.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    dataList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val item = dataSnapshot.getValue(Surat::class.java)!!
                        val reference = item.sort.toString()
                        if (pref.getValues("level").equals("Warga")) {
                            if (reference.equals(name)) {
                                dataList.add(item)
                            }
                        } else {
                            dataList.add(item)
                        }
                    }
                    showData()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MailActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            })

            bind.etSearch.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    mDatabase.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            dataList.clear()
                            for (dataSnapshot in snapshot.children) {
                                val item = dataSnapshot.getValue(Surat::class.java)!!
                                val sort = item.sort.toString()
                                val reference = item.subject!!.toLowerCase(Locale.ROOT)
                                if (reference.contains(s.toString().toLowerCase(Locale.ROOT))) {
                                    if (pref.getValues("level").equals("Warga")) {
                                        if (sort.equals(name)) {
                                            dataList.add(item)
                                        }
                                    } else {
                                        dataList.add(item)
                                    }
                                }
                            }
                            showData()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@MailActivity, error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        } else {
            net.isOffline()
        }

        if (pref.getValues("level").equals("Warga")) {
            bind.btnMail.visibility = View.GONE
            bind.btnAdd.visibility = View.VISIBLE
        } else {
            bind.btnMail.visibility = View.VISIBLE
            bind.btnAdd.visibility = View.GONE
        }

        bind.btnAdd.setOnClickListener {
            pref.setValues("action", "add")
            startActivity(Intent(this, AddOutgoingMailActivity::class.java))
        }

        bind.btnMailIn.setOnClickListener {
            startActivity(Intent(this, IncomingMailActivity::class.java))
        }

        bind.btnMailOut.setOnClickListener {
            startActivity(Intent(this, OutgoingMailActivity::class.java))
        }
    }

    private fun showData() {
        dataList.reverse()
        bind.recyclerView.layoutManager = LinearLayoutManager(this)
        bind.recyclerView.adapter = OutgoingMailAdapter(dataList){
            if (pref.getValues("level").equals("Admin")){
                moreAction()
            } else {
                startActivity(Intent(this, DetailOutgoingMailActivity::class.java))
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
            startActivity(Intent(this, AddOutgoingMailActivity::class.java))
        }

        val detail = view.findViewById<LinearLayout>(R.id.view)
        detail.setOnClickListener {
            dialog.menu().dismiss()
            startActivity(Intent(this, DetailOutgoingMailActivity::class.java))
        }

        val delete = view.findViewById<LinearLayout>(R.id.delete)
        delete.setOnClickListener {
            mDatabase.child(pref.getValues("id").toString()).removeValue()
            Toast.makeText(this, "Data telah dihapus", Toast.LENGTH_SHORT).show()
            dialog.menu().dismiss()
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, HomeActivity::class.java))
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}