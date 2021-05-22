package com.mobile.spanduk.ui.home.info

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.mobile.spanduk.R
import com.mobile.spanduk.model.Informasi
import com.mobile.spanduk.utils.Preferences

class InformasiAdapter(
    private var data: List<Informasi>,
    private var listener: (Informasi) -> Unit
) : RecyclerView.Adapter<InformasiAdapter.LeagueViewHolder>() {

    lateinit var ContextAdapter: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeagueViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        ContextAdapter = parent.context
        val inflatedView = layoutInflater.inflate(R.layout.row_item_informasi, parent, false)

        return LeagueViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: LeagueViewHolder, position: Int) {
        holder.bindItem(data[position], listener, ContextAdapter)
    }

    override fun getItemCount(): Int = data.size

    class LeagueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.tv_title)
        val detail = view.findViewById<TextView>(R.id.tv_detail)
        val expand = view.findViewById<ImageView>(R.id.btn_action)
        val keterangan = view.findViewById<TextView>(R.id.tv_keterangan)
        val content = view.findViewById<ConstraintLayout>(R.id.itemContent)

        fun bindItem(data: Informasi, listener: (Informasi) -> Unit, context: Context) {
            val pref = Preferences(context)
            title.text = data.title
            detail.text = data.detail
            keterangan.text = data.detail

            expand.setOnClickListener {
                if (detail.visibility == View.GONE) {
                    TransitionManager.beginDelayedTransition(content, AutoTransition())
                    keterangan.visibility = View.GONE
                    detail.visibility = View.VISIBLE
                    expand.setImageResource(R.drawable.ic_expand_less)
                } else {
                    TransitionManager.beginDelayedTransition(content, AutoTransition())
                    keterangan.visibility = View.VISIBLE
                    detail.visibility = View.GONE
                    expand.setImageResource(R.drawable.ic_expand_more_blue)
                }
            }

            itemView.setOnClickListener {
                pref.setValues("id", data.id.toString())
                pref.setValues("title", data.title.toString())
                pref.setValues("detail", data.detail.toString())
                listener(data)
            }
        }
    }
}