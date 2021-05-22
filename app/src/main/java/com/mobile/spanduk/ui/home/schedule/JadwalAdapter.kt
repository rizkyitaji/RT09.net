package com.mobile.spanduk.ui.home.schedule

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.mobile.spanduk.R
import com.mobile.spanduk.model.Jadwal
import com.mobile.spanduk.utils.BottomSheetDialog
import com.mobile.spanduk.utils.Preferences

class JadwalAdapter(
    private var data: List<Jadwal>,
    private var listener: (Jadwal) -> Unit
) : RecyclerView.Adapter<JadwalAdapter.LeagueViewHolder>() {

    lateinit var ContextAdapter: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeagueViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        ContextAdapter = parent.context
        val inflatedView = layoutInflater.inflate(R.layout.row_item_schedule, parent, false)

        return LeagueViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: LeagueViewHolder, position: Int) {
        holder.bindItem(data[position], listener, ContextAdapter)
    }

    override fun getItemCount(): Int = data.size

    class LeagueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date = view.findViewById<TextView>(R.id.tv_date)
        val time = view.findViewById<TextView>(R.id.tv_time)
        val place = view.findViewById<TextView>(R.id.tv_place)
        val edit = view.findViewById<ImageView>(R.id.btn_edit)
        val activity = view.findViewById<TextView>(R.id.tv_name)
        val delete = view.findViewById<ImageView>(R.id.btn_delete)
        val action = view.findViewById<LinearLayout>(R.id.btn_action)

        fun bindItem(data: Jadwal, listener: (Jadwal) -> Unit, context: Context) {
            val pref = Preferences(context)
            val id = data.id.toString()
            val name = data.name.toString()
            activity.text = name
            date.text = data.date
            time.text = data.time
            place.text = data.place

            if (pref.getValues("level").equals("Admin")) {
                action.visibility = View.VISIBLE
            }

            val ref = FirebaseDatabase.getInstance().getReference("Jadwal").child(id)

            edit.setOnClickListener {
                pref.setValues("action", "edit")
                pref.setValues("id", data.id.toString())
                pref.setValues("name", data.name.toString())
                pref.setValues("date", data.date.toString())
                pref.setValues("time", data.time.toString())
                pref.setValues("place", data.place.toString())
                listener(data)
            }

            delete.setOnClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Apakah Anda yakin ingin menghapus jadwal kegiatan $name ?")
                    .setPositiveButton("Ya") { _, _ ->
                        ref.removeValue()
                        Toast.makeText(context, "Data telah dihapus", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Tidak") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }
    }
}