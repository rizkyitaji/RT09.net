package com.mobile.spanduk.ui.home.report

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mobile.spanduk.R
import com.mobile.spanduk.model.Report
import com.mobile.spanduk.utils.Connection
import com.mobile.spanduk.utils.Preferences

class ReportAdapter(private var data: List<Report>,
                    private var listener: (Report) -> Unit)
    : RecyclerView.Adapter<ReportAdapter.LeagueViewHolder>() {

    lateinit var ContextAdapter: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeagueViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        ContextAdapter = parent.context
        val inflatedView = layoutInflater.inflate(R.layout.row_item_report, parent, false)

        return LeagueViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: LeagueViewHolder, position: Int) {
        holder.bindItem(data[position], listener, ContextAdapter)
    }

    override fun getItemCount(): Int = data.size

    class LeagueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.tv_name)
        val date = view.findViewById<TextView>(R.id.tv_date)
        val detail = view.findViewById<TextView>(R.id.tv_detail)
        val status = view.findViewById<TextView>(R.id.tv_status)
        val content = view.findViewById<CardView>(R.id.cv_content)
        val expand = view.findViewById<ImageView>(R.id.btn_expand)
        val approve = view.findViewById<Button>(R.id.btn_approve)
        val reject = view.findViewById<Button>(R.id.btn_reject)
        val expandableView = view.findViewById<ConstraintLayout>(R.id.expandableView)

        fun bindItem(data: Report, listener: (Report) -> Unit, context: Context) {
            val net = Connection(context)
            val pref = Preferences(context)
            val id = data.id.toString()
            name.text = data.name
            date.text = data.date
            detail.text = data.detail
            status.text = data.status

            if (pref.getValues("level").equals("Ketua RT")) {
                if (data.status.equals("Menunggu")) {
                    expand.setOnClickListener {
                        if (expandableView.visibility == View.GONE) {
                            TransitionManager.beginDelayedTransition(content, AutoTransition())
                            expandableView.visibility = View.VISIBLE
                            expand.setImageResource(R.drawable.ic_expand_less)
                        } else {
                            TransitionManager.beginDelayedTransition(content, AutoTransition())
                            expandableView.visibility = View.GONE
                            expand.setImageResource(R.drawable.ic_expand_more_blue)
                        }
                    }
                } else {
                    expand.setImageResource(R.drawable.ic_keyboard_arrow_right)
                }
            } else {
                expand.setImageResource(R.drawable.ic_keyboard_arrow_right)
            }

            val ref = FirebaseDatabase.getInstance().getReference("Report").child(id)

            approve.setOnClickListener {
                if (net.isOnline()) {
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            ref.child("status").setValue("Diterima")

                            expandableView.visibility = View.GONE
                            TransitionManager.beginDelayedTransition(content, AutoTransition())

                            Toast.makeText(context, "Laporan telah diterima", Toast.LENGTH_SHORT).show()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    net.isOffline()
                }
            }

            reject.setOnClickListener {
                if (net.isOnline()) {
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            ref.child("status").setValue("Ditolak")

                            expandableView.visibility = View.GONE
                            TransitionManager.beginDelayedTransition(content, AutoTransition())

                            Toast.makeText(context, "Laporan telah ditolak", Toast.LENGTH_SHORT).show()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    net.isOffline()
                }
            }

            itemView.setOnClickListener {
                pref.setValues("id", id)
                pref.setValues("name", data.name.toString())
                pref.setValues("date", data.date.toString())
                pref.setValues("uri", data.photo.toString())
                pref.setValues("detail", data.detail.toString())
                pref.setValues("action", "edit")
                listener(data)
            }
        }
    }
}