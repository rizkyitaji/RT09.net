package com.mobile.spanduk.ui.home.schedule

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.mobile.spanduk.R
import com.mobile.spanduk.databinding.ActivityJadwalBinding
import com.mobile.spanduk.model.Jadwal
import com.mobile.spanduk.utils.BottomSheetDialog
import com.mobile.spanduk.utils.Connection
import com.mobile.spanduk.utils.Preferences
import java.util.*
import kotlin.collections.ArrayList

class JadwalActivity : AppCompatActivity() {

    lateinit var id: String
    lateinit var net: Connection
    lateinit var pref: Preferences
    lateinit var mDatabase: DatabaseReference
    private var dataList = ArrayList<Jadwal>()
    private lateinit var bind: ActivityJadwalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityJadwalBinding.inflate(layoutInflater)
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

        mDatabase = FirebaseDatabase.getInstance().getReference("Jadwal")
        val ref = mDatabase.orderByChild("date")

        if (net.isOnline()) {
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    dataList.clear()
                    for (dataSnapshot in snapshot.children) {
                        dataList.add(dataSnapshot.getValue(Jadwal::class.java)!!)
                    }
                    showData()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@JadwalActivity, ""+error.message, Toast.LENGTH_SHORT).show()
                }
            })

            bind.etSearch.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    ref.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            dataList.clear()
                            for (dataSnapshot in snapshot.children) {
                                val item = dataSnapshot.getValue(Jadwal::class.java)!!
                                val reference = item.name!!.toLowerCase(Locale.ROOT)
                                if (reference.contains(s.toString().toLowerCase(Locale.ROOT))) {
                                    dataList.add(item)
                                }
                            }
                            showData()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@JadwalActivity, ""+error.message, Toast.LENGTH_SHORT).show()
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
            addSchedule()
        }
    }

    private fun showData() {
        bind.recyclerView.layoutManager = LinearLayoutManager(this)
        bind.recyclerView.adapter = JadwalAdapter(dataList){
            if (pref.getValues("action").equals("edit")){
                addSchedule()
            }
        }
    }

    private fun addSchedule() {
        val dialog = BottomSheetDialog(this)
        val view = dialog.build(R.layout.layout_add_schedule)

        val close = view.findViewById<ImageView>(R.id.iv_close)
        val editName = view.findViewById<EditText>(R.id.et_name)
        val editDate = view.findViewById<TextView>(R.id.tv_date)
        val editTime = view.findViewById<TextView>(R.id.tv_time)
        val editPlace = view.findViewById<EditText>(R.id.et_place)
        val submit = view.findViewById<Button>(R.id.btn_submit)

        close.setOnClickListener {
            dialog.menu().dismiss()
        }

        if (pref.getValues("action").equals("edit")){
            id = pref.getValues("id").toString()
            editName.setText(pref.getValues("name"))
            editDate.text = pref.getValues("date")
            editTime.text = pref.getValues("time")
            editPlace.setText(pref.getValues("place"))
        } else {
            id = mDatabase.push().key.toString()
        }

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        editDate.setOnClickListener {
            val datePicker = DatePickerDialog(this, { _, mYear, mMonth, mDay ->
                    val textDate = "$mDay-${mMonth + 1}-$mYear"
                    editDate.text = textDate
                }, year, month, day)
            datePicker.show()
        }

        editTime.setOnClickListener {
            val timePicker =  TimePickerDialog(this,
                android.R.style.Theme_Material_Dialog_MinWidth, { _, hourOfDay, minute ->
                    calendar.set(0, 0, 0, hourOfDay, minute)
                    editTime.text = DateFormat.format("HH:mm", calendar)
                }, 24, 0, true)
//            timePicker.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            timePicker.show()
        }

        submit.setOnClickListener {
            val name = editName.text.toString()
            val date = editDate.text.toString()
            val time = editTime.text.toString()
            val place = editPlace.text.toString()

            if (name.equals("")){
                editName.error = "Lengkapi isian terlebih dahulu!"
                editName.requestFocus()
            } else if (date.equals("Pilih Tanggal")){
                Toast.makeText(this, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_SHORT).show()
            } else if (time.equals("Pilih Waktu")){
                Toast.makeText(this, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_SHORT).show()
            } else if (place.equals("")){
                editPlace.error = "Lengkapi isian terlebih dahulu!"
                editPlace.requestFocus()
            } else {
                if (net.isOnline()){
                    val ref = mDatabase.child(id)
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            ref.child("id").setValue(id)
                            ref.child("name").setValue(name)
                            ref.child("date").setValue(date)
                            ref.child("time").setValue(time)
                            ref.child("place").setValue(place)
                            Toast.makeText(this@JadwalActivity, "Data telah diperbarui", Toast.LENGTH_SHORT).show()
                            dialog.menu().dismiss()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@JadwalActivity, ""+error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    net.isOffline()
                }
            }
        }
    }
}