package com.mobile.spanduk.ui.home.mail.incoming

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobile.spanduk.R
import com.mobile.spanduk.model.Mail
import com.mobile.spanduk.utils.Preferences

class IncomingMailAdapter(private var data: List<Mail>,
                          private var listener: (Mail) -> Unit)
    : RecyclerView.Adapter<IncomingMailAdapter.LeagueViewHolder>() {

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
        val no = view.findViewById<TextView>(R.id.tv_id)
        val date = view.findViewById<TextView>(R.id.tv_date)
        val colon = view.findViewById<TextView>(R.id.colon1)
        val tgl = view.findViewById<TextView>(R.id.tv_tanggal)
        val textSubject = view.findViewById<TextView>(R.id.two)
        val textStatus = view.findViewById<TextView>(R.id.three)
        val subject = view.findViewById<TextView>(R.id.tv_detail)
        val status = view.findViewById<TextView>(R.id.tv_status)
        val expand = view.findViewById<ImageView>(R.id.btn_expand)

        fun bindItem(data: Mail, listener: (Mail) -> Unit, context: Context) {
            val pref = Preferences(context)
            no.text = data.no
            tgl.text = data.dateReceived
            date.text = data.mailingDate
            subject.text = data.subject
            textSubject.text = context.getString(R.string.perihal)

            colon.visibility = View.GONE
            status.visibility = View.GONE
            textStatus.visibility = View.GONE
            expand.setImageResource(R.drawable.ic_keyboard_arrow_right)

            itemView.setOnClickListener {
                pref.setValues("action", "edit")
                pref.setValues("no", data.no.toString())
                pref.setValues("etc", data.etc.toString())
                pref.setValues("from", data.from.toString())
                pref.setValues("detail", data.detail.toString())
                pref.setValues("subject", data.subject.toString())
                pref.setValues("tgl", data.dateReceived.toString())
                pref.setValues("date", data.mailingDate.toString())
                listener(data)
            }
        }
    }
}