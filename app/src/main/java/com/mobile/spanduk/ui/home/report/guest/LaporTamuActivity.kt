package com.mobile.spanduk.ui.home.report.guest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.mobile.spanduk.R
import com.mobile.spanduk.databinding.ActivityLaporTamuBinding
import com.mobile.spanduk.model.Report
import com.mobile.spanduk.ui.home.HomeActivity
import com.mobile.spanduk.ui.home.report.AddReportActivity
import com.mobile.spanduk.ui.home.report.DetailReportActivity
import com.mobile.spanduk.ui.home.report.ReportAdapter
import com.mobile.spanduk.utils.AlertDialog
import com.mobile.spanduk.utils.Connection
import com.mobile.spanduk.utils.Preferences
import java.util.*

class LaporTamuActivity : AppCompatActivity() {

    lateinit var net: Connection
    lateinit var mDatabase: Query
    lateinit var pref: Preferences
    lateinit var ref: DatabaseReference
    private var dataList = ArrayList<Report>()
    private lateinit var bind: ActivityLaporTamuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityLaporTamuBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val net = Connection(this)
        pref = Preferences(this)

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

        ref = FirebaseDatabase.getInstance().getReference("Report")

        if (net.isOnline()) {
            mDatabase = ref.orderByChild("type").equalTo("Lapor Tamu")

            mDatabase.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    dataList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val item = dataSnapshot.getValue(Report::class.java)!!
                        val reference = item.name.toString()
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
                    Toast.makeText(this@LaporTamuActivity, ""+error.message, Toast.LENGTH_SHORT).show()
                }
            })

            bind.etSearch.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    mDatabase.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            dataList.clear()
                            for (dataSnapshot in snapshot.children) {
                                val item = dataSnapshot.getValue(Report::class.java)!!
                                val sort = item.name.toString()
                                val reference = item.detail!!.toLowerCase(Locale.ROOT)
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
                            Toast.makeText(this@LaporTamuActivity, ""+error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        } else {
            net.isOffline()
        }

        if (!pref.getValues("level").equals("Warga")) {
            bind.btnAdd.visibility = View.GONE
        }

        bind.btnAdd.setOnClickListener {
            pref.setValues("action", "add")
            startActivity(Intent(this, AddReportActivity::class.java)
                .putExtra("report", "tamu"))
        }
    }

    private fun showData() {
        dataList.reverse()
        bind.recyclerView.layoutManager = LinearLayoutManager(this)
        bind.recyclerView.adapter = ReportAdapter(dataList){
            if (pref.getValues("level").equals("Admin")) {
                moreAction()
            } else {
                startActivity(Intent(this, DetailReportActivity::class.java)
                    .putExtra("report", "tamu"))
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
            startActivity(Intent(this, AddReportActivity::class.java)
                    .putExtra("report", "tamu"))
        }

        val detail = view.findViewById<LinearLayout>(R.id.view)
        detail.setOnClickListener {
            dialog.menu().dismiss()
            startActivity(Intent(this, DetailReportActivity::class.java)
                .putExtra("report", "tamu"))
        }

        val delete = view.findViewById<LinearLayout>(R.id.delete)
        delete.setOnClickListener {
            val id = pref.getValues("id").toString()
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