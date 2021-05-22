package com.mobile.spanduk.ui.home.user

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mobile.spanduk.R
import com.mobile.spanduk.model.Users
import com.mobile.spanduk.utils.Preferences
import com.mobile.spanduk.utils.ShowImage

class UserAdapter(
    private var data: List<Users>,
    private var listener: (Users) -> Unit
) : RecyclerView.Adapter<UserAdapter.LeagueViewHolder>() {

    lateinit var ContextAdapter: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeagueViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        ContextAdapter = parent.context
        val inflatedView = layoutInflater.inflate(R.layout.row_item_user, parent, false)

        return LeagueViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: LeagueViewHolder, position: Int) {
        holder.bindItem(data[position], listener, ContextAdapter)
    }

    override fun getItemCount(): Int = data.size

    class LeagueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textName = view.findViewById<TextView>(R.id.tv_name)
        val textPhone = view.findViewById<TextView>(R.id.tv_phone)
        val profile = view.findViewById<ImageView>(R.id.iv_profile)
        val action = view.findViewById<ImageView>(R.id.btn_action)

        fun bindItem(data: Users, listener: (Users) -> Unit, context: Context) {
            val img = ShowImage(context)
            val pref = Preferences(context)
            val uri = data.photo.toString()
            val name = data.name.toString()
            val id = data.username.toString()
            val phone = data.phone.toString()
            val wa = data.whatsapp.toString()
            val pass = data.password.toString()
            textName.text = name
            textPhone.text = phone

            val builder = AlertDialog.Builder(context)
            val mDatabase = FirebaseDatabase.getInstance().getReference("Users")

            if (!uri.equals("")) {
                Glide.with(context)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profile)
            }

            profile.setOnClickListener {
                img.show(uri, "blue")
            }

            val ref = mDatabase.child(id)
            if (pref.getValues("page").equals("edit")) {
                pref.setValues("check", "0")
                action.setImageResource(R.drawable.ic_check_box_outline)
                action.setOnClickListener {
                    if (pref.getValues("check").equals("0")) {
                        pref.setValues("check", "1")
                        action.setImageResource(R.drawable.ic_check_box)
                        val query = mDatabase.orderByChild("level").equalTo("Ketua RT")
                        query.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (dataSnapshot in snapshot.children) {
                                    val uname = dataSnapshot.child("username").value.toString()
                                    pref.setValues("id", id)
                                    pref.setValues("uname", uname)
                                    listener(data)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        if (pref.getValues("id").equals(id)) {
                            pref.setValues("check", "0")
                            action.setImageResource(R.drawable.ic_check_box_outline)
                        }
                    }
                }
            } else {
                action.setOnClickListener {
                    builder.setMessage("Apakah Anda yakin ingin menghapus user $name ?")
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

            itemView.setOnClickListener {
                pref.setValues("uri", uri)
                pref.setValues("uname", id)
                pref.setValues("name", name)
                pref.setValues("pass", pass)
                pref.setValues("phone", phone)
                pref.setValues("whatsapp", wa)
                pref.setValues("user", "Warga")
                listener(data)
            }
        }
    }
}