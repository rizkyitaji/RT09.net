package com.mobile.spanduk.ui.home.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobile.spanduk.R
import com.mobile.spanduk.model.Warga
import com.mobile.spanduk.utils.Preferences

class DaftarWargaAdapter(private var data: List<Warga>,
                         private var listener: (Warga) -> Unit)
    : RecyclerView.Adapter<DaftarWargaAdapter.LeagueViewHolder>() {

    lateinit var ContextAdapter: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeagueViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        ContextAdapter = parent.context
        val inflatedView = layoutInflater.inflate(R.layout.row_item_daftar_warga, parent, false)

        return LeagueViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: LeagueViewHolder, position: Int) {
        holder.bindItem(data[position], listener, ContextAdapter)
    }

    override fun getItemCount(): Int = data.size

    class LeagueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textName = view.findViewById<TextView>(R.id.tv_name)
        val textPhone = view.findViewById<TextView>(R.id.tv_phone)
        val textAddress = view.findViewById<TextView>(R.id.tv_address)

        fun bindItem(data: Warga, listener: (Warga) -> Unit, context: Context) {
            val pref = Preferences(context)
            textName.text = data.name
            textPhone.text = data.phone
            textAddress.text = data.address

            itemView.setOnClickListener {
                pref.setValues("kk", data.kk.toString())
                pref.setValues("nik", data.nik.toString())
                pref.setValues("ttl", data.ttl.toString())
                pref.setValues("job", data.job.toString())
                pref.setValues("name", data.name.toString())
                pref.setValues("phone", data.phone.toString())
                pref.setValues("gender", data.gender.toString())
                pref.setValues("address", data.address.toString())
                pref.setValues("religion", data.religion.toString())
                pref.setValues("education", data.education.toString())
                listener(data)
            }
        }
    }
}