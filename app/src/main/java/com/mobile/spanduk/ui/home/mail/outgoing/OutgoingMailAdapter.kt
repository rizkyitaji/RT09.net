package com.mobile.spanduk.ui.home.mail.outgoing

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import com.mobile.spanduk.model.Surat
import com.mobile.spanduk.utils.AlertDialog
import com.mobile.spanduk.utils.Connection
import com.mobile.spanduk.utils.Preferences

class OutgoingMailAdapter(private var data: List<Surat>,
                          private var listener: (Surat) -> Unit)
    : RecyclerView.Adapter<OutgoingMailAdapter.LeagueViewHolder>() {

    lateinit var ContextAdapter: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeagueViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        ContextAdapter = parent.context
        val inflatedView = layoutInflater.inflate(R.layout.row_item_mail, parent, false)

        return LeagueViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: LeagueViewHolder, position: Int) {
        holder.bindItem(data[position], listener, ContextAdapter)
    }

    override fun getItemCount(): Int = data.size

    class LeagueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val getID = view.findViewById<TextView>(R.id.tv_id)
        val date = view.findViewById<TextView>(R.id.tv_date)
        val tgl = view.findViewById<TextView>(R.id.tv_tanggal)
        val detail = view.findViewById<TextView>(R.id.tv_detail)
        val status = view.findViewById<TextView>(R.id.tv_status)
        val content = view.findViewById<CardView>(R.id.cv_content)
        val expand = view.findViewById<ImageView>(R.id.btn_expand)
        val approve = view.findViewById<Button>(R.id.btn_approve)
        val done = view.findViewById<Button>(R.id.btn_done)
        val expandableView = view.findViewById<ConstraintLayout>(R.id.expandableView)

        fun bindItem(data: Surat, listener: (Surat) -> Unit, context: Context) {
            val net = Connection(context)
            val pref = Preferences(context)
            val id = data.id.toString()
            val no = data.no.toString()
            tgl.text = data.tgl
            date.text = data.date
            detail.text = data.subject
            status.text = data.status

            if (pref.getValues("mail").equals("warga")) {
                getID.text = data.name
            } else {
                if (no.equals("")) {
                    getID.text = "-"
                } else {
                    getID.text = no
                }
            }

            if (pref.getValues("level").equals("Ketua RT")) {
                if (!data.status.equals("Selesai")) {
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

            val ref = FirebaseDatabase.getInstance().getReference("Surat Keluar").child(id)

            approve.setOnClickListener {
                if (net.isOnline()) {
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            ref.child("status").setValue("Belum Selesai")

                            expandableView.visibility = View.GONE
                            TransitionManager.beginDelayedTransition(content, AutoTransition())

                            Toast.makeText(context, "Pengajuan surat telah diterima", Toast.LENGTH_SHORT).show()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    net.isOffline()
                }
            }

            done.setOnClickListener {
                if (net.isOnline()) {
                    val dialog = AlertDialog(context)
                    val view = dialog.build(R.layout.layout_mail_number)

                    val editNum = view.findViewById<EditText>(R.id.et_mailNum)
                    val submit = view.findViewById<Button>(R.id.btn_submit)

                    submit.setOnClickListener {
                        val number = editNum.text.toString().trim()

                        if (number.equals("")) {
                            editNum.error = "Lengkapi isian terlebih dahulu!"
                            editNum.requestFocus()
                        } else {
                            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    ref.child("no").setValue(number)
                                    ref.child("status").setValue("Selesai")

                                    expandableView.visibility = View.GONE
                                    TransitionManager.beginDelayedTransition(content, AutoTransition())

                                    Toast.makeText(context, "Surat telah selesai dibuat", Toast.LENGTH_SHORT).show()
                                    dialog.menu().dismiss()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }
                } else {
                    net.isOffline()
                }
            }

            itemView.setOnClickListener {
                pref.setValues("id", id)
                pref.setValues("no", no)
                pref.setValues("ttl", data.ttl.toString())
                pref.setValues("job", data.job.toString())
                pref.setValues("nik", data.nik.toString())
                pref.setValues("name", data.name.toString())
                pref.setValues("date", data.date.toString())
                pref.setValues("gender", data.gender.toString())
                pref.setValues("address", data.address.toString())
                pref.setValues("subject", data.subject.toString())
                pref.setValues("religion", data.religion.toString())
                pref.setValues("action", "edit")
                listener(data)
            }
        }
    }
}