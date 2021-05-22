package com.mobile.spanduk.ui.home.info

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.mobile.spanduk.R
import com.mobile.spanduk.databinding.ActivityInformasiBinding
import com.mobile.spanduk.model.Informasi
import com.mobile.spanduk.utils.*
import java.util.*
import kotlin.collections.ArrayList

class InformasiActivity : AppCompatActivity() {

    lateinit var id: String
    lateinit var net: Connection
    lateinit var pref: Preferences
    lateinit var mDatabase: DatabaseReference
    private var dataList = ArrayList<Informasi>()
    private lateinit var bind: ActivityInformasiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityInformasiBinding.inflate(layoutInflater)
        setContentView(bind.root)

        net = Connection(this)
        pref = Preferences(this)

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

        mDatabase = FirebaseDatabase.getInstance().getReference("Informasi")

        if (net.isOnline()) {
            mDatabase.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    dataList.clear()
                    for (dataSnapshot in snapshot.children) {
                        dataList.add(dataSnapshot.getValue(Informasi::class.java)!!)
                    }
                    showData()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@InformasiActivity, ""+error.message, Toast.LENGTH_SHORT).show()
                }
            })

            bind.etSearch.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    mDatabase.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            dataList.clear()
                            for (dataSnapshot in snapshot.children) {
                                val item = dataSnapshot.getValue(Informasi::class.java)!!
                                val reference = item.title!!.toLowerCase(Locale.ROOT)
                                if (reference.contains(s.toString().toLowerCase(Locale.ROOT))) {
                                    dataList.add(item)
                                }
                            }
                            showData()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@InformasiActivity, ""+error.message, Toast.LENGTH_SHORT).show()
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
            pref.setValues("action", "add")
            addInfo()
        }
    }

    private fun showData() {
        bind.recyclerView.layoutManager = LinearLayoutManager(this)
        bind.recyclerView.addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(this, R.drawable.divider)))
        bind.recyclerView.adapter = InformasiAdapter(dataList){
            if (pref.getValues("level").equals("Admin")){
                moreAction()
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
            pref.setValues("action", "edit")
            addInfo()
        }

        val detail = view.findViewById<LinearLayout>(R.id.view)
        detail.visibility = View.GONE

        val delete = view.findViewById<LinearLayout>(R.id.delete)
        delete.setOnClickListener {
            mDatabase.child(pref.getValues("id").toString()).removeValue()
            Toast.makeText(this, "Data telah dihapus", Toast.LENGTH_SHORT).show()
            dialog.menu().dismiss()
        }
    }

    private fun addInfo() {
        val dialog = BottomSheetDialog(this)
        val view = dialog.build(R.layout.layout_add_info)

        val close = view.findViewById<ImageView>(R.id.iv_close)
        val submit = view.findViewById<Button>(R.id.btn_submit)
        val editTitle = view.findViewById<EditText>(R.id.et_title)
        val editDetail = view.findViewById<EditText>(R.id.et_detail)

        close.setOnClickListener {
            dialog.menu().dismiss()
        }

        if (pref.getValues("action").equals("edit")){
            id = pref.getValues("id").toString()
            editTitle.setText(pref.getValues("title"))
            editDetail.setText(pref.getValues("detail"))
        } else {
            id = mDatabase.push().key.toString()
        }

        submit.setOnClickListener {
            val title = editTitle.text.toString()
            val detail = editDetail.text.toString()

            if (title.equals("")){
                editTitle.error = "Lengkapi isian terlebih dahulu!"
                editTitle.requestFocus()
            } else if (detail.equals("")){
                editDetail.error = "Lengkapi isian terlebih dahulu!"
                editDetail.requestFocus()
            } else {
                if (net.isOnline()){
                    val ref = mDatabase.child(id)
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            ref.child("id").setValue(id)
                            ref.child("title").setValue(title)
                            ref.child("detail").setValue(detail)
                            Toast.makeText(this@InformasiActivity, "Data telah diperbarui", Toast.LENGTH_SHORT).show()
                            dialog.menu().dismiss()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@InformasiActivity, ""+error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    net.isOffline()
                }
            }
        }
    }
}