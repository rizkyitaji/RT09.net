package com.mobile.spanduk.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Button
import com.mobile.spanduk.R

class Connection(val context: Context) {
    fun isOnline() : Boolean {
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val allNetwork = connectivity.allNetworks
        for (network in allNetwork){
            val networkCapabilities = connectivity.getNetworkCapabilities(network)
            if (networkCapabilities != null) {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                    return true
                }
            }
        }
        return false
    }

    fun isOffline() {
        val dialog = AlertDialog(context)
        val view = dialog.build(R.layout.layout_offline)
        dialog.menu().setCancelable(false)

        val ok = view.findViewById<Button>(R.id.btn_okay)
        ok.setOnClickListener {
            dialog.menu().dismiss()
        }
    }

}